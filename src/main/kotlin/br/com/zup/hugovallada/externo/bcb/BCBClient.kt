package br.com.zup.hugovallada.externo.bcb

import br.com.zup.hugovallada.pix.DeletarChavePixRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client

@Client("\${urls.apis.bcb}")
interface BCBClient {

    @Post("/api/v1/pix/keys")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    fun criarChave(@Body bcbChavePixRequest: CreatePixKeyRequest): HttpResponse<CreatePixKeyResponse>

    @Delete("/api/v1/pix/keys/{key}")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    fun deletarChave(@Body bcbDeletarChavePixRequest: DeletePixKeyRequest, @PathVariable key: String): HttpResponse<DeletePixKeyResponse>



}