package br.com.zup.academy.armando

import io.micronaut.runtime.Micronaut.*
fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("br.com.zup.academy.armando")
		.start()
}

