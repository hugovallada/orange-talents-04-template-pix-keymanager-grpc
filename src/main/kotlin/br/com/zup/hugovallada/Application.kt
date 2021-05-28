package br.com.zup.hugovallada

import io.micronaut.runtime.Micronaut.*
fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("br.com.zup.hugovallada")
		.start()
}

