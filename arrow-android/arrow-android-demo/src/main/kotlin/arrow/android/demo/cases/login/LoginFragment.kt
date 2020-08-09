package arrow.android.demo.cases.login

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import arrow.android.binding.core.checkedStateChanges
import arrow.android.binding.core.clicks
import arrow.android.binding.material.endIconClicks
import arrow.android.demo.R
import arrow.android.demo.cases.login.LoginEvent.ChangePasswordVisibility
import arrow.android.demo.cases.login.LoginEvent.Exit
import arrow.android.demo.cases.login.LoginEvent.Login
import arrow.android.demo.cases.login.LoginEvent.TogglePasswordVisibility
import arrow.android.demo.util.effectLog
import arrow.fx.coroutines.stream.Stream
import arrow.fx.coroutines.stream.compile
import arrow.fx.coroutines.stream.parJoinUnbounded
import kotlinx.android.synthetic.main.fragment_login.*

/**
 * This fragment is a show case of a non trivial form without state restoring mechanism (i.e. viewModel)
 */
class LoginFragment : Fragment(R.layout.fragment_login) {

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    lifecycleScope.launchWhenCreated {
      stateMachine.start(events.effectLog()) { state -> render(state) }
        .effectLog()
        .compile().drain()
    }
  }

  private val stateMachine = StateMachine<LoginState, LoginEvent>(LoginState()) { state, event ->
    state.transition(event)
  }

  private val events by lazy {
    Stream(
      password_layout.endIconClicks().map { TogglePasswordVisibility },
      checkbox.checkedStateChanges().map { ChangePasswordVisibility(it) },
      checkbox_label.clicks().map { TogglePasswordVisibility },
      login_button.clicks().map { Login(email_input.text, password_input.text) },
      exit_button.clicks().map { Exit }
    ).parJoinUnbounded()
  }

  private fun render(state: LoginState) {
    render(state.ui)
    render(state.data)
  }

  private fun render(ui: LoginState.Ui) {
    when (ui) {
      is LoginState.Ui.Active -> {
        checkbox.isChecked = ui.showPassword
        password_input.transformationMethod = if (ui.showPassword) null else hidePasswordToken
      }
      is LoginState.Ui.Exit -> findNavController().popBackStack()
    }
  }

  private fun render(data: LoginState.Data) {
    when (data) {
      is LoginState.Data.Idle -> views.showOnly(login_form)
      is LoginState.Data.Progress -> views.showOnly(progress)
      is LoginState.Data.Success -> {
        views.showOnly(exit_layout)
        success_text.render(data.user)
      }
      is LoginState.Data.Error -> {
        views.showOnly(login_form, error_message)
        error_message.text = data.message
      }
    }
  }

  private val views: List<View> by lazy { listOf(login_form, progress, exit_layout, error_message) }

}

private fun TextView.render(user: User) {
  text = resources.getString(R.string.welcome_user, user.name)
}

private fun List<View>.showOnly(vararg views: View) = forEach { view ->
  view.post { view.visibility = if (view in views) View.VISIBLE else View.GONE }
}

private val hidePasswordToken = PasswordTransformationMethod()

