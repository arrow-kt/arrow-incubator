package arrow.android.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(R.layout.activity_main) {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setSupportActionBar(toolbar)
    toolbar.title = title

    setupActionBarWithNavController(mainNavController)
  }

  override fun onSupportNavigateUp(): Boolean {
    return mainNavController.navigateUp() || super.onSupportNavigateUp()
  }

  private val mainNavController by lazy {
    supportFragmentManager.findFragmentById(R.id.main_container)!!.findNavController()
  }

}
