package dev.olog.presentation.fragment_recently_added.di

import dagger.Subcomponent
import dagger.android.AndroidInjector
import dev.olog.presentation.dagger.PerFragment
import dev.olog.presentation.fragment_recently_added.RecentlyAddedFragment

@Subcomponent(modules = arrayOf(
        RecentlyAddedFragmentModule::class
))
@PerFragment
interface RecentlyAddedFragmentSubComponent : AndroidInjector<RecentlyAddedFragment> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<RecentlyAddedFragment>() {

        abstract fun module(module: RecentlyAddedFragmentModule): Builder

        override fun seedInstance(instance: RecentlyAddedFragment) {
            module(RecentlyAddedFragmentModule(instance))
        }
    }

}