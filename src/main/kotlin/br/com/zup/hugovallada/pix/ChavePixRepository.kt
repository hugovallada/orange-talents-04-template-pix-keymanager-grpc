package br.com.zup.hugovallada.pix

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, UUID> {
    fun existsByChave(chave : String): Boolean
    fun existsByIdAndClienteId(id: UUID, clienteId: UUID): Boolean
    fun existsByIdAndContaCpfDoTitular(id: UUID, cpf: String): Boolean
    fun findByChave(chave: String): Optional<ChavePix>
    fun findAllByClienteId(clienteId: UUID): List<ChavePix>
}