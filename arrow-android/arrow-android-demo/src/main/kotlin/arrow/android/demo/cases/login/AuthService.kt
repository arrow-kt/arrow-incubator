package arrow.android.demo.cases.login

import kotlinx.coroutines.delay

suspend fun request(query: AuthQuery): User {
  delay(1_000)
  if (query.email.toString() == "j.bond@sis.gov.uk" && query.password.toString() == "supersecret")
    return User("Mr. Bond")
  else
    throw AuthException()
}

data class User(val name: Username)

typealias Username = String

data class AuthQuery(val email: Email, val password: Password)

class AuthException : Exception()
