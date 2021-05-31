package br.com.zup.hugovallada.externo

import br.com.zup.hugovallada.conta.DadosContaResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import java.net.http.HttpResponse

@Client("\${urls.apis.itau-erp}")
interface ItauERPClient {


    @Get("/api/v1/clientes/{clienteId}/contas{?tipo}")
    fun buscarClientePorConta(@PathVariable idCliente: String, @QueryValue tipo: String) : HttpResponse<DadosContaResponse>

}