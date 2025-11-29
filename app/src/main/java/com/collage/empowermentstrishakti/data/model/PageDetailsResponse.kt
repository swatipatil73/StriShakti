package com.collage.empowermentstrishakti.data.model

data class PageDetailsResponse(
    val currentPageMembers: List<Member> = emptyList(),
    val hasNextPage: Boolean = false,
    val totalPages: Int = 0,
    val pageSize: Int = 0,
    val nextPageNo: Int = 0,
    val currentPage: Int = 0,
    val pageAbout: PageAbout? = null,
    val postDetails: List<PostDetail> = emptyList(),
    val totalElements: Int = 0
)

data class Member(
    val userProfileImagePath: String? = null,
    val userFirstName: String? = null,
    val userlastName: String? = null,
    val userId: Int = 0
)

data class PageAbout(
    val pagesId: Int = 0,
    val pageName: String? = null,
    val pageDescription: String? = null,
    val pageCoverProfileImagePath: String? = null,
    val adminId: Int = 0,
    val adminUserFirstName: String? = null,
    val adminUserLastName: String? = null,
    val adminUserProfileImagePath: String? = null,
    val pageCreatedAt: String? = null,
    val puuid: String? = null,
    val linkUrlName: String? = null,
    val linkUrl: String? = null,
    val pageFollowed: Boolean = false
)

data class PostDetail(
    val postId: Int = 0,
    val userId: Int = 0,
    val userProfileImageUrl: String? = null,
    val userName: String? = null,
    val postImageURl: String? = null,
    val postType: String? = null,
    val videoThumbnailUrl: String? = null,
    val postCreatedAt: String? = null,
    val postName: String? = null,
    val totalCountOFReact: Int = 0,
    val totalComments: Int? = null,
    val userReactStatus: Boolean = false,
    val postUploadedAt: String? = null,
    val userUUID: String? = null,
    val description: String? = null,
    val mediaFiles: List<Any>? = null,
    val commentsAndReacts: List<Any>? = null,
    val reachCount: Int = 0,
    val viewCount: Int = 0,
    val postSaved: Boolean = false,
    val tempUuid: String? = null
)
