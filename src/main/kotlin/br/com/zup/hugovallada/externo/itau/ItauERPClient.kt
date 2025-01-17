package br.com.zup.hugovallada.externo.itau

import br.com.zup.hugovallada.conta.DadosContaResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${urls.apis.itau-erp}")
interface ItauERPClient {


    @Get("/api/v1/clientes/{idCliente}/contas?tipo={tipo}")
    fun buscarClientePorConta(@PathVariable idCliente: String, @QueryValue tipo:String) : DadosContaResponse?

    @Get("/api/v1/clientes/{idCliente}")
    fun buscarClientePorId(@PathVariable idCliente: String) : DadosClienteResponseClient?

}