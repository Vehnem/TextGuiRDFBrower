package de.v122.rdf

import org.apache.jena.riot.system.IRIResolver
import org.scalatest.FunSuite

class JenaTests  extends FunSuite {

  test("IRIResolver") {

    val iri = "http://dbpedia.org/resource/Berlin"
    val ir2 = "http://dbpedia.org/resource  /Berlin"
    val ir3 = "http://dbpedia.org/resource|/Berlin"
    val ir4 = "http://dbpedia.org/resource"

    println(IRIResolver.checkIRI(iri))
    println(IRIResolver.checkIRI(ir2))
    println(IRIResolver.checkIRI(ir4))
  }

}
