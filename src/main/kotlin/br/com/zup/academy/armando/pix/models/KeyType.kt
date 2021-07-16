package br.com.zup.academy.armando.pix.models

import br.com.zup.academy.armando.core.handler.exceptions.CustomBadRequestException
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator

enum class KeyType {
    CPF {
        override fun validate(key: String?) {
            if (key.isNullOrBlank() || !CPFValidator().run {
                    initialize(null)
                    isValid(key, null)
                }) {
                throw CustomBadRequestException("key é obrigatória e formato esperado deve ser um CPF válido.")
            }
        }
    }, EMAIL {
        override fun validate(key: String?) {
            if (key.isNullOrBlank() || !key.matches("(?:[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])".toRegex())) {
                throw CustomBadRequestException("key é obrigatória e formato esperado deve ser seu@email.com")
            }
        }
    }, CELULAR {
        override fun validate(key: String?) {
            if (key.isNullOrBlank() || !key.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())) {
                throw CustomBadRequestException("chave é obrigatória e formato esperado deve ser +5585988714077")
            }
        }
    }, RANDOMICA {
        override fun validate(key: String?) {
            if (!key.isNullOrBlank()) throw CustomBadRequestException("Para tipo chave RANDOMICA não deve ser informada uma chave.")
        }
    };

    abstract fun validate(key: String?)
}