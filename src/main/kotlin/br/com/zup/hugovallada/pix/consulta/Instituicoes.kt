package br.com.zup.hugovallada.pix.consulta

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.nio.file.Files
import java.nio.file.Paths

class Instituicoes {

    companion object{
        private var nomes = mutableMapOf<String, String>()
        init {
            val read = Files.newBufferedReader(Paths.get(".\\ParticipantesSTRport.csv"))
            val parser = CSVParser(read, CSVFormat.DEFAULT)
            parser.forEach { dado ->
                nomes[dado.get(0)] = dado.get(1).trim()
            }
        }

        fun nome(participant: String): String {
            return nomes[participant]!!
        }
    }

}
