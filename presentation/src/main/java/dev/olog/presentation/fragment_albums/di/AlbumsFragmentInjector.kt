package dev.olog.presentation.fragment_albums.di

import android.support.v4.app.Fragment
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.support.FragmentKey
import dagger.multibindings.IntoMap
import dev.olog.presentation.fragment_albums.AlbumsFragment

@Module(subcomponents = arrayOf(AlbumsFragmentSubComponent::class))
abstract class AlbumsFragmentInjector {

    @Binds
    @IntoMap
    @FragmentKey(AlbumsFragment::class)
    internal abstract fun injectorFactory(builder: AlbumsFragmentSubComponent.Builder)
            : AndroidInjector.Factory<out Fragment>

}
