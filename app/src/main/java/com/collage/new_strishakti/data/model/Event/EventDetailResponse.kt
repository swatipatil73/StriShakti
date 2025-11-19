package com.collage.new_strishakti.data.model.Event

import com.google.gson.annotations.SerializedName

// EventDetailResponse.kt
data class EventDetailResponse(
    @SerializedName("about") val about: List<EventAbout> = emptyList(),
    @SerializedName("discussion") val discussion: List<DiscussionItem> = emptyList()
)

data class EventAbout(
    @SerializedName("eventAddress") val eventAddress: String?,
    @SerializedName("eventId") val eventId: Int?,
    @SerializedName("eventMode") val eventMode: String?,
    @SerializedName("districtName") val districtName: String?,
    @SerializedName("endDate") val endDate: String?,
    @SerializedName("postImageUrl") val postImageUrl: String?,
    @SerializedName("categoryName") val categoryName: String?,
    @SerializedName("eventNotify") val eventNotify: String?,
    @SerializedName("virtualEventLink") val virtualEventLink: String?,
    @SerializedName("postName") val postName: String?,
    @SerializedName("eventDescription") val eventDescription: String?,
    @SerializedName("eventName") val eventName: String?,
    @SerializedName("startTime") val startTime: String?,
    @SerializedName("endTime") val endTime: String?,
    @SerializedName("isParticipant") var isParticipant: Boolean = false,
    @SerializedName("startDate") val startDate: String?
)

data class DiscussionItem(
    @SerializedName("postId") val postId: Long,
    @SerializedName("userId") val userId: Long,
    @SerializedName("userProfileImageUrl") val userProfileImageUrl: String?,
    @SerializedName("userName") val userName: String?,
    @SerializedName("postImageURl") val postImageURl: String?,
    @SerializedName("postType") val postType: String?,
    @SerializedName("videoThumbnailUrl") val videoThumbnailUrl: String?,
    @SerializedName("postCreatedAt") val postCreatedAt: String?,
    @SerializedName("postName") val postName: String?,
    @SerializedName("totalCountOFReact") val totalCountOFReact: Int?,
    @SerializedName("totalComments") val totalComments: Int?,
    @SerializedName("userReactStatus") val userReactStatus: Boolean?,
    @SerializedName("postUploadedAt") val postUploadedAt: String?,
    @SerializedName("userUUID") val userUUID: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("mediaFiles") val mediaFiles: Any?,
    @SerializedName("commentsAndReacts") val commentsAndReacts: List<Any>?,
    @SerializedName("reachCount") val reachCount: Int?,
    @SerializedName("viewCount") val viewCount: Int?,
    @SerializedName("postSaved") val postSaved: Boolean?
)