package org.metabrainz.android.presentation.features.release_list

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import org.metabrainz.android.data.repository.LookupRepository
import org.metabrainz.android.data.sources.api.entities.CoverArt
import org.metabrainz.android.data.sources.api.entities.mbentity.Release
import org.metabrainz.android.util.Resource.Status.SUCCESS
import javax.inject.Inject

@HiltViewModel
class ReleaseListViewModel @Inject constructor(val repository: LookupRepository) : ViewModel() {
    val releaseList = MutableLiveData<List<Release>>()

    fun setReleases(releases: List<Release>) {
        releaseList.value = releases
    }

    fun fetchCoverArtForRelease(release: Release): LiveData<CoverArt> {
        return liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
            val result = repository.fetchCoverArt(release.mbid!!)
            if (result.status == SUCCESS) {
                emit(result.data!!)
            }
        }
    }

}