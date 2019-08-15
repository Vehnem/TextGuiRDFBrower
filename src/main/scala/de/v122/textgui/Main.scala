package de.v122.textgui

import java.io.File
import java.util
import java.util.regex.Pattern

import com.googlecode.lanterna.gui2._
import com.googlecode.lanterna.gui2.dialogs.{TextInputDialogBuilder, TextInputDialogResultValidator}
import com.googlecode.lanterna.input.{KeyStroke, KeyType}
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.{DefaultTerminalFactory, Terminal}
import com.googlecode.lanterna.{TerminalSize, TextCharacter, TextColor}
import org.apache.jena.query.{QueryExecutionFactory, QueryFactory}
import org.apache.jena.riot.system.IRIResolver

import scala.collection.mutable.ArrayBuffer
import scala.util.Random
import scala.collection.JavaConversions._

object Main {

  def main(args: Array[String]): Unit = {

    val defaultTerminalFactory = new DefaultTerminalFactory()

    var terminal: Terminal = null
    try {

      terminal = defaultTerminalFactory.createTerminal()
    } catch {

      case e: Exception => e.printStackTrace()
    }

    val terminalSize: TerminalSize = terminal.getTerminalSize

    val screen = new TerminalScreen(terminal)
    screen.startScreen()

    // Create window to hold the panel
    val window = new BasicWindow()
    window.setHints(util.Arrays.asList(Window.Hint.FULL_SCREEN))

    // Create gui and start gui
    val gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));

    import com.googlecode.lanterna.gui2.Borders
    import com.googlecode.lanterna.gui2.Direction
    import com.googlecode.lanterna.gui2.LinearLayout

    val mainPanel = new Panel()
    mainPanel.setLayoutManager(new LinearLayout(Direction.VERTICAL))

    val centerPanel = new Panel(new LinearLayout(Direction.HORIZONTAL))
//    centerPanel.setLayoutManager()

    val textInputDialog = new TextInputDialogBuilder()
      .setTitle("Title")
      .setDescription("Enter a single number")
      .setValidator(new TextInputDialogResultValidator {
        override def validate(s: String): String = {
          val valid = IRIResolver.checkIRI(s)
          if ( s == null || s == "" ) "should not be empty"
          else if (valid) s"not an valid iri$s"
          else null
        }
      }).build()

    screen.doResizeIfNecessary()
    val endpointUrl: endpointUrl = textInputDialog.showDialog(gui)


    mainPanel.addComponent(new TextBox(new TerminalSize(tColumns(terminal)-2,1),endpointUrl))

    val leftPanel = new Panel(new LinearLayout(Direction.VERTICAL))
    leftPanel.setPreferredSize()
    leftPanel.setSize(new TerminalSize((terminalSize.getColumns/2)-4,1))
    centerPanel.addComponent(leftPanel)
    leftPanel.setSize(leftPanel.calculatePreferredSize())

    getPublisher("https://databus.dbpedia.org/repo/sparql").foreach(
      publisher => leftPanel.addComponent(newTextBox((terminalSize.getColumns/2)-4,1,publisher))
    )

    val rightPanel = new Panel(new LinearLayout(Direction.VERTICAL))
//    rightPanel.addComponent(newTextBox(10,1,"Hello"))

    rightPanel.addComponent(newTextBox((terminalSize.getColumns/2)-4,1,".."))
    new File("./").listFiles().foreach(
      file => rightPanel.addComponent(newTextBox((terminalSize.getColumns/2)-4,1,file.getName))
    )

    centerPanel.addComponent(rightPanel)

    mainPanel.addComponent(centerPanel)

    window.setComponent(mainPanel)
    gui.addWindowAndWait(window)
  }

  def newTextBox(columns: Int, rows: Int, text: String): TextBox = {
    val tb = new TextBox(new TerminalSize(columns,rows),text)
    tb.setReadOnly(true)
    tb
  }

  type endpointUrl = String
  def getPublisher(endpointUrl: endpointUrl): Array[String] = {

    val publishers = new ArrayBuffer[String]()

    val query = QueryFactory.create(
      s"""PREFIX dct:   <http://purl.org/dc/terms/>
         |
         |SELECT DISTINCT ?publisher {
         | ?dataid dct:publisher ?publisher
         |}
       """.stripMargin)

    val resultSet = QueryExecutionFactory.sparqlService(endpointUrl,query).execSelect()

    for ( qs <- resultSet ) {
      System.err.println(qs.getResource("publisher").getURI)
      publishers.append(qs.getResource("publisher").getURI)
    }

    publishers.toArray
  }

  def tColumns(terminal: Terminal): Int = {
    terminal.getTerminalSize.getColumns
  }

  def tRows(terminal: Terminal): Int = {
    terminal.getTerminalSize.getRows
  }
}
