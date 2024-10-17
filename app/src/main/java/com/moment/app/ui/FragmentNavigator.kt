package com.moment.app.ui

import android.os.Bundle
import android.util.Log
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle

class FragmentNavigator(
    private val mFragmentManager: FragmentManager,
    adapter: FragmentNavigatorInterface,
    @IdRes containerViewId: Int
) {
    private val mAdapter: FragmentNavigatorInterface = adapter

    @IdRes
    private val mContainerViewId = containerViewId
    var currentPosition: Int = -1
        private set
    private var mDefaultPosition = 0

    init {
        if (mFragmentManager.fragments.size > 1) {
            this.removeAllFragment(true)
        }
    }

    fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            this.currentPosition =
                savedInstanceState.getInt("extra_current_position", this.mDefaultPosition)
        }
    }

    fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("extra_current_position", this.currentPosition)
    }

    @JvmOverloads
    fun showFragment(position: Int, reset: Boolean = false, allowingStateLoss: Boolean = true) {
        try {
            this.currentPosition = position
            val transaction = mFragmentManager.beginTransaction()
            val count: Int = mAdapter.getCount()

            for (i in 0 until count) {
                if (i != position) {
                    this.hide(i, transaction)
                }
            }

            if (reset) {
                this.remove(position, transaction)
                this.add(position, transaction)
            } else {
                this.show(position, transaction)
            }

            if (allowingStateLoss) {
                transaction.commitNowAllowingStateLoss()
            } else {
                transaction.commitNow()
            }
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            val e = throwable
        }
    }

    @JvmOverloads
    fun resetFragments(position: Int = this.currentPosition, allowingStateLoss: Boolean = false) {
        try {
            this.currentPosition = position
            val transaction = mFragmentManager.beginTransaction()
            this.removeAll(transaction)
            this.add(position, transaction)
            if (allowingStateLoss) {
                transaction.commitAllowingStateLoss()
            } else {
                transaction.commit()
            }
        } catch (throwable: Throwable) {
            val e = throwable
        }
    }

    @JvmOverloads
    fun removeAllFragment(allowingStateLoss: Boolean = false) {
        try {
            val transaction = mFragmentManager.beginTransaction()
            this.removeAll(transaction)
            if (allowingStateLoss) {
                transaction.commitNowAllowingStateLoss()
            } else {
                transaction.commitNow()
            }
        } catch (ex: Exception) {
            val e = ex
        }
    }

    val currentFragment: Fragment?
        get() = this.getFragment(this.currentPosition)

    fun getFragment(position: Int): Fragment? {
        val tag: String = mAdapter.getTag(position)
        return mFragmentManager.findFragmentByTag(tag)
    }

    private fun show(position: Int, transaction: FragmentTransaction) {
        val tag: String = mAdapter.getTag(position)
        val fragment = mFragmentManager.findFragmentByTag(tag)
        if (fragment == null) {
            this.add(position, transaction)
        } else {
            transaction.show(fragment)
            transaction.setMaxLifecycle(fragment, Lifecycle.State.RESUMED)
        }
    }

    private fun hide(position: Int, transaction: FragmentTransaction) {
        val tag: String = mAdapter.getTag(position)
        val fragment = mFragmentManager.findFragmentByTag(tag)
        if (fragment != null) {
            transaction.hide(fragment).setMaxLifecycle(fragment, Lifecycle.State.STARTED)
        }
    }

    private fun add(position: Int, transaction: FragmentTransaction) {
        val fragment: Fragment = mAdapter.onCreateFragment(position)
        val tag: String = mAdapter.getTag(position)
        transaction.add(this.mContainerViewId, fragment, tag)
        transaction.setMaxLifecycle(fragment, Lifecycle.State.RESUMED)
        this.onPageResumeTrack(fragment, true)
    }

    private fun onPageResumeTrack(fragment: Fragment?, isNewAdded: Boolean) {
        if (fragment != null) {
//            val page: SeaPage? = fragment.javaClass.getAnnotation(SeaPage::class.java) as SeaPage?
//            if (page != null) {
//                val isNestedTabPage =
//                    page.isTabPage() && page.skip() && fragment is SeaFragmentStateAdapter.ISeaStateFragment
//                if (isNestedTabPage) {
//                    this.handleNestedTabTrack(fragment, isNewAdded)
//                } else {
//                    this.handleNormalTabTrack(fragment, isNewAdded)
//                }
//            }
        }
    }

    private fun handleNestedTabTrack(fragment: Fragment, isNewAdded: Boolean) {
        if (isNewAdded) {
//            fragment.getLifecycle().addObserver(LifecycleObserver { source, event ->
//                try {
//                    if (event === Lifecycle.Event.ON_RESUME || event === Lifecycle.Event.ON_PAUSE) {
//                        (fragment as SeaFragmentStateAdapter.ISeaStateFragment).getFragmentStateAdapter()
//                            .onStateChanged(fragment, source, event)
//                    }
//                } catch (var4: Exception) {
//                    val e = var4
//                }
//            })
        }
    }

    private fun handleNormalTabTrack(fragment: Fragment, isNewAdded: Boolean) {
        if (isNewAdded) {
        //    fragment.lifecycle.addObserver(LifecycleObserver { source, event ->
//                try {
//                    if (event === Lifecycle.Event.ON_RESUME) {
//                        SeaLog.i("Navigator fragment trackPageResume ==> " + fragment.javaClass.simpleName)
//                        SeaPageManager.getInstance().resumePage(fragment)
//                    } else if (event === Lifecycle.Event.ON_PAUSE) {
//                        SeaLog.i("Navigator fragment trackPagePause ==> " + fragment.javaClass.simpleName)
//                        SeaPageManager.getInstance().pausePage(fragment)
//                    }
//                } catch (var4: Exception) {
//                    val e = var4
//                    SeaLog.e("handleNormalTabTrack ==> " + e.message)
//                }
          //  })
        }
    }

    private fun hideAll(allowingStateLoss: Boolean) {
        try {
            val transaction = mFragmentManager.beginTransaction()
            val count: Int = mAdapter.getCount()

            for (i in 0 until count) {
                this.hide(i, transaction)
            }

            if (allowingStateLoss) {
                transaction.commitNowAllowingStateLoss()
            } else {
                transaction.commitNow()
            }
        } catch (throwable: Throwable) {
        }
    }

    private fun removeAll(transaction: FragmentTransaction) {
        val count: Int = mAdapter.getCount()

        for (i in 0 until count) {
            this.remove(i, transaction)
        }
    }

    private fun remove(position: Int, transaction: FragmentTransaction) {
        val tag: String = mAdapter.getTag(position)
        val fragment = mFragmentManager.findFragmentByTag(tag)
        if (fragment != null) {
            transaction.remove(fragment)
        }
    }

    fun setDefaultPosition(defaultPosition: Int) {
        this.mDefaultPosition = defaultPosition
        if (this.currentPosition == -1) {
            this.currentPosition = defaultPosition
        }
    }

    companion object {
        private const val EXTRA_CURRENT_POSITION = "extra_current_position"
    }
}
