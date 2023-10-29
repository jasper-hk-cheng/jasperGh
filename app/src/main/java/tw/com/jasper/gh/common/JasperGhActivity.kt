package tw.com.jasper.gh.common

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class JasperGhActivity<T : ViewBinding> : AppCompatActivity(), ActivityInitContract<T> {

    private lateinit var viewBinding: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = inflateViewBinding().apply {
            setContentView(root)
        }
        initSOP()
    }

    override fun getViewBinding(): T = viewBinding
}

interface ActivityInitContract<T : ViewBinding> {
    // default method
    fun initSOP() {
        installView(getViewBinding())
        initView(getViewBinding())
    }

    fun inflateViewBinding(): T
    fun getViewBinding(): T

    fun installView(viewBinding: T)
    fun initView(viewBinding: T)
}