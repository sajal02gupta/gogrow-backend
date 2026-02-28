package com.gogrow

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GogrowApplication

fun main(args: Array<String>) {
	runApplication<GogrowApplication>(*args)
}
