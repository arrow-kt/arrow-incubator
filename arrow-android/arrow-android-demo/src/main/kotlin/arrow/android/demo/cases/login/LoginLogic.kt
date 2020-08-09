package arrow.android.demo.cases.login

import arrow.android.demo.util.castAs
import arrow.fx.coroutines.stream.Stream
import arrow.fx.coroutines.stream.append
import arrow.fx.coroutines.stream.handleErrorWith

// TODO(pabs): Update to use lenses
fun LoginState.transition(event: LoginEvent): Stream<LoginState> = when (event) {
  is LoginEvent.TogglePasswordVisibility -> Stream(copy(ui = ui.togglePassword()))
  is LoginEvent.ChangePasswordVisibility -> Stream(copy(ui = ui.applyPassword(event.showPassword)))
  is LoginEvent.Login -> Stream(copy(data = LoginState.Data.Progress)).append {
    Stream.effect { request(AuthQuery(event.email, event.password)) }
      .map { copy(data = LoginState.Data.Success(it)) }
      .handleErrorWith { Stream(copy(data = LoginState.Data.Error("Opps, that didn't work"))) }
  }
  LoginEvent.Exit -> Stream(copy(ui = LoginState.Ui.Exit))
}

sealed class LoginEvent {
  object TogglePasswordVisibility : LoginEvent()
  data class ChangePasswordVisibility(val showPassword: ShowPassword) : LoginEvent()
  data class Login(val email: Email, val password: Password) : LoginEvent()
  object Exit : LoginEvent()
}

data class LoginState(
  val ui: Ui = Ui.Active(),
  val data: Data = Data.Idle
) {

  sealed class Data {
    object Idle : Data()
    object Progress : Data()
    data class Success(val user: User) : Data()
    data class Error(val message: ErrorMessage) : Data()
  }

  sealed class Ui {
    data class Active(val showPassword: ShowPassword = false) : Ui()
    object Exit : Ui()
    
    fun togglePassword(): Ui = castAs<Active>()?.let { Active(!it.showPassword) } ?: this

    fun applyPassword(showPassword: ShowPassword): Ui = when(this) {
      is Active -> Active(showPassword)
      is Exit -> Exit
    }
  }
}

typealias Email = CharSequence
typealias Password = CharSequence
typealias ErrorMessage = CharSequence
typealias ShowPassword = Boolean
