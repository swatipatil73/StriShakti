package com.collage.new_strishakti.data.model.post

sealed class HomeFeedItem {
    data class PostItem(val post: PostData) : HomeFeedItem()
    data class ReelSection(val reels: List<ReelData>) : HomeFeedItem()
    data class AdItem(val ad: AdPost) : HomeFeedItem()
    data class AnnouncementItem(val announcement: Announcement) : HomeFeedItem()
    object LoadingItem : HomeFeedItem()

}


