package com.collage.new_strishakti.data.model.post

data class HomeFeedResult(
    val items: List<HomeFeedItem>,
    val nextCursor: Long?,
    val hasNextPage: Boolean,
    val announcement: Announcement?
)

data class HomePostPage(
    val items: List<HomeFeedItem>,
    val nextCursor: Long?,
    val hasNextPage: Boolean
)

