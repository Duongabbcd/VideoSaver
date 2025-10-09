package com.example.videosaver.advance.di.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.videosaver.advance.ui.HistoryViewModel
import com.example.videosaver.advance.ui.browser.BrowserViewModel
import com.example.videosaver.advance.ui.browser.detectedVideos.GlobalVideoDetectionModel
import com.example.videosaver.advance.ui.browser.detectedVideos.VideoDetectionTabViewModel
import com.example.videosaver.advance.ui.browser.hometab.BrowserHomeViewModel
import com.example.videosaver.advance.ui.proxies.ProxiesViewModel
import com.example.videosaver.advance.ui.setting.SettingsViewModel
import com.example.videosaver.advance.ui.webtab.WebTabViewModel
import com.example.videosaver.screen.home.MainViewModel
import com.example.videosaver.screen.player.VideoPlayerViewModel
import com.example.videosaver.utils.advance.util.ViewModelFactory
import com.example.videosaver.viewmodel.video.VideoViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import javax.inject.Singleton

@Module(includes = [AppModule::class])
abstract class ViewModelModule {

    @Singleton
    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

//    @Binds
//    @IntoMap
//    @ViewModelKey(SplashViewModel::class)
//    abstract fun bindSplashViewModel(viewModel: SplashViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindMainViewModel(viewModel: MainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BrowserViewModel::class)
    abstract fun bindBrowserViewModel(viewModel: BrowserViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(VideoPlayerViewModel::class)
    abstract fun bindVideoPlayerViewModel(viewModel: VideoPlayerViewModel): ViewModel

//    @Binds
//    @IntoMap
//    @ViewModelKey(ProgressViewModel::class)
//    abstract fun bindProgressViewModel(viewModel: ProgressViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(VideoViewModel::class)
    abstract fun bindVideoViewModel(viewModel: VideoViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    abstract fun bindSettingsViewModel(viewModel: SettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HistoryViewModel::class)
    abstract fun bindHistoryViewModel(viewModel: HistoryViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProxiesViewModel::class)
    abstract fun bindProxiesViewModel(viewModel: ProxiesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(WebTabViewModel::class)
    abstract fun bindWebTabViewModel(viewModel: WebTabViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BrowserHomeViewModel::class)
    abstract fun bindBrowserHomeViewModel(viewModel: BrowserHomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GlobalVideoDetectionModel::class)
    abstract fun bindVideoDetectionAlgViewModel(viewModel: GlobalVideoDetectionModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(VideoDetectionTabViewModel::class)
    abstract fun bindVideoDetectionDetectedViewModel(viewModel: VideoDetectionTabViewModel): ViewModel
}
