package br.com.zup.academy.armando.core.handler

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
class CustomErrorBody(
    val statusCode: Int,
    val name: String,
    val description: String,
    val details: List<CustomErrorDetail>?
)

class CustomErrorDetail(
    val description: String
)
