package com.collage.empowermentstrishakti.data.network



import com.collage.empowermentstrishakti.data.model.Comment.CommentModel
import com.collage.empowermentstrishakti.data.model.Comment.CommentResponse
import com.collage.empowermentstrishakti.data.model.CreatePagePostRequest
import com.collage.empowermentstrishakti.data.model.CreatePagePostResponse
import com.collage.empowermentstrishakti.data.model.Event.CreateEventResponse
import com.collage.empowermentstrishakti.data.model.Event.DeleteResponse
import com.collage.empowermentstrishakti.data.model.Event.EventCategoryResponse
import com.collage.empowermentstrishakti.data.model.Event.EventDetailResponse
import com.collage.empowermentstrishakti.data.model.Event.EventResponse
import com.collage.empowermentstrishakti.data.model.Event.JoinEventResponse
import com.collage.empowermentstrishakti.data.model.Event.ParticipantResponse
import com.collage.empowermentstrishakti.data.model.FriendListResponse
import com.collage.empowermentstrishakti.data.model.Groups.GroupDetailsResponse
import com.collage.empowermentstrishakti.data.model.Groups.GroupListResponse
import com.collage.empowermentstrishakti.data.model.Notification.MarkReadResponse
import com.collage.empowermentstrishakti.data.model.Notification.NotificationResponse
import com.collage.empowermentstrishakti.data.model.PageDetailsResponse
import com.collage.empowermentstrishakti.data.model.PageListResponse
import com.collage.empowermentstrishakti.data.model.Profile.UpdateUserResponse
import com.collage.empowermentstrishakti.data.model.Profile.UserProfileResponse
import com.collage.empowermentstrishakti.data.model.Reel.ReelResponse
import com.collage.empowermentstrishakti.data.model.Reel.ReelUploadResponse
import com.collage.empowermentstrishakti.data.model.SavedPost.CreatePageRequest
import com.collage.empowermentstrishakti.data.model.SavedPost.SavedPostResponse
import com.collage.empowermentstrishakti.data.model.friend.FriendRequestResponse
import com.collage.empowermentstrishakti.data.model.friend.SearchFriendsResponse
import com.collage.empowermentstrishakti.data.model.post.AdsResponse
import com.collage.empowermentstrishakti.data.model.post.AnnouncementResponse
import com.collage.empowermentstrishakti.data.model.post.CommonResponse
import com.collage.empowermentstrishakti.data.model.post.CreatePostResponse
import com.collage.empowermentstrishakti.data.model.post.HomeResponse
import com.collage.empowermentstrishakti.data.model.post.ReelsResponse
import com.collage.empowermentstrishakti.data.model.regi.College
import com.collage.empowermentstrishakti.data.model.regi.Department
import com.collage.empowermentstrishakti.data.model.regi.District
import com.collage.empowermentstrishakti.data.model.regi.LoginRequest
import com.collage.empowermentstrishakti.data.model.regi.LoginResponse
import com.collage.empowermentstrishakti.data.model.regi.RegisterRequest
import com.collage.empowermentstrishakti.data.model.regi.School
import com.collage.empowermentstrishakti.data.model.regi.State
import com.collage.empowermentstrishakti.data.model.regi.StreamItem
import com.collage.empowermentstrishakti.data.model.regi.StudyCentre
import com.collage.empowermentstrishakti.data.model.regi.Taluka
import com.collage.empowermentstrishakti.data.model.regi.UniversityResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Query

interface ApiService {


    @POST("ssakti/users/user/userRegister")
    suspend fun registerUser(@Body request: RegisterRequest): Response<ResponseBody>

    @GET("public/api/state/getAllStates")
    suspend fun getAllStates(): Response<List<State>>

    @GET("public/api/district/getDistrictsByStateId/{stateId}")
    suspend fun getDistricts(@Path("stateId") stateId: Int): Response<List<District>>

    @GET("public/api/taluka/talukaByDitrictId/{districtId}")
    suspend fun getTalukas(@Path("districtId") districtId: Int): Response<List<Taluka>>


    @GET("api/university/list")
    suspend fun getUniversities(): Response<UniversityResponse>




    @GET("api/college/university/id/{universityId}")
    suspend fun getCollegesByUniversity(@Path("universityId") universityId: Int): Response<List<College>>

    @GET("api/schools/university/{universityId}")
    suspend fun getSchoolsByUniversity(@Path("universityId") universityId: Int): Response<List<School>>

    @GET("api/study-centre/university/id/{universityId}")
    suspend fun getStudyCentresByUniversity(@Path("universityId") universityId: Int): Response<List<StudyCentre>>

    @GET("api/departments/getAll")
    suspend fun getAllDepartments(): Response<List<Department>>

    @GET("api/stream-updated/getAll")
    suspend fun getAllStreams(): Response<List<StreamItem>>


    @Headers("Content-Type: application/json")
    @POST("ssakti/users/user/userLogin")
    fun loginUser(@Body request: LoginRequest): Call<LoginResponse>

    //post

    // 1) Announcement
    // Example: /ssakti/users/user/getAnnouncement/SUP_ADMIN_ANNOUNCEMENT?postCategory=...
    @GET("ssakti/users/user/getAnnouncement/{postCategory}")
    suspend fun getAnnouncement(
        @Path("postCategory") postCategory: String,
        @Header("Authorization") token: String
    ): Response<AnnouncementResponse>



    // 2) Ads - SUP_ADMIN
    @GET("sskati/users/posts/getAds/SUP_ADMIN")
    suspend fun getAdsSuperAdmin(
        @Query("postCategory") postCategory: String?,
        @Header("Authorization") token: String
    ): Response<AdsResponse>

    // 3) Ads - ADMIN
    @GET("sskati/users/posts/getAds/ADMIN")
    suspend fun getAdsAdmin(
        @Query("postCategory") postCategory: String?,
        @Header("Authorization") token: String
    ): Response<AdsResponse>

    // 4) Home posts (paginated)
    // Example: /ssakti/users/home/home/v1/{userId}?cursor=0&size=5
    @GET("ssakti/users/home/home/v1/{userId}")
    suspend fun getHomePosts(
        @Path("userId") userId: Long,
        @Query("cursor") cursor: Long,
        @Query("size") size: Int,
        @Header("Authorization") token: String
    ): Response<HomeResponse>

    // 5) Reels (paginated)
    // /ssakti/users/reels/getAllReels?page=0&size=10
    @GET("ssakti/users/reels/getAllReels")
    suspend fun getAllReels(
        @Query("page") page: Int,
        @Query("size") size: Int=5,
        @Header("Authorization") token: String
    ): Response<ReelsResponse>


    @Multipart
    @POST("sskati/users/posts/addPost/{userId}")
    suspend fun addPost(
        @Path("userId") userId: Int,
        @Header("Authorization") token: String, // pass token dynamically
        @Part("postName") postName: RequestBody,
        @Part("postType") postType: RequestBody,
        @Part("videoThumbnailUrl") videoThumbnailUrl: RequestBody,
        @Part postImage: List<MultipartBody.Part>?
    ): Response<CreatePostResponse>

//deleet
        @DELETE("sskati/users/posts/deletPost/{postId}")
        suspend fun deletePost(
            @Path("postId") postId: Int,
            @Header("Authorization") bearer: String
        ): Response<CommentResponse>

        //save  https://dev.api.strishakti.org/ssakti/users/savepost/savePost/352/1446
        @POST("ssakti/users/savepost/savePost/{userId}/{postId}")
        suspend fun savePost(
            @Path("userId") userId: Int,
            @Path("postId") postId: Int,
            @Header("Authorization") token: String
        ): Response<CommonResponse>


        //repeot   ssakti/users/dispute/raiseDispute


    @POST("ssakti/users/dispute/raiseDispute/{userId}/{postId}/{disputeTitleId}")
    suspend fun reportPost(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int,
        @Path("postId") postId: Int,
        @Path("disputeTitleId") disputeTitleId: Int,
        @Body body: Map<String, String>
    ): Response<CommonResponse>
    //like

    // ✅ LIKE POST
    @POST("ssakti/users/postreact/addReactOnPost/{userId}/{postId}")
    suspend fun addReactOnPost(
        @Path("userId") userId: String,
        @Path("postId") postId: String,
        @Body body: RequestBody,
        @Header("Authorization") token: String
    ): Response<CommonResponse>

    // ✅ UNLIKE POST
    @DELETE("ssakti/users/postreact/deleteReactOnPost/{userId}/{postId}")
    suspend fun deleteReactOnPost(
        @Path("userId") userId: String,
        @Path("postId") postId: String,
        @Header("Authorization") token: String
    ): Response<CommonResponse>



    @GET("ssakti/users/postcomment/getCommentsAndReacts/{postId}")
    suspend fun getComments(
        @Header("Authorization") token: String,
        @Path("postId") postId: Int
    ): Response<CommentResponse>

    @POST("ssakti/users/postcomment/addCommentOnPost/{userId}/{postId}")

    suspend fun addComment(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int,
        @Path("postId") postId: Int,
        @Body body: Map<String, String>
    ): Response<CommentModel>

    @POST("ssakti/users/postcomment/addCommentOnComment/{userId}/{postId}/{commentId}")


    suspend fun addReply(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int,
        @Path("postId") postId: Int,
        @Path("commentId") commentId: Int,
        @Body body: Map<String, String>
    ): Response<CommonResponse> // or ApiResponse if that’s your model


    @DELETE("ssakti/users/postcomment/deleteParentComment/{userId}/{postCommentId}")
    suspend fun deleteParentComment(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int,
        @Path("postCommentId") postCommentId: Int
    ): Response<CommonResponse>

    //reel
    @GET("ssakti/users/reels/getAllReels")
    suspend fun getReels(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): ReelResponse


    @Multipart
    @POST("ssakti/users/reels/addReel/{userId}")
    suspend fun addReel(
        @Path("userId") userId: Int,
        @Header("Authorization") token: String,
        @Part("postName") postName: RequestBody,
        @Part("postType") postType: RequestBody,
        @Part("videoThumbnailUrl") videoThumbnailUrl: RequestBody,
        @Part postImage: List<MultipartBody.Part>?
    ): Response<ReelUploadResponse>


    @DELETE("ssakti/users/reels/deletReel/{reelId}")
    suspend fun deleteReel(
        @Path("reelId") reelId: Int,
        @Header("Authorization") token: String
    ): Response<CommonResponse>

    //FriendListResponse


    @GET("ssakti/users/friendrequest/getFriendsList/{userId}")
    suspend fun getFriendsList(
        @Path("userId") userId: Int,
        @Header("Authorization") token: String
    ): Response<FriendListResponse>

    @GET("ssakti/users/user/searchUser/{userName}")
    suspend fun searchFriends(
        @Path("userName") userName: String,
        @Header("Authorization") token: String
    ): Response<SearchFriendsResponse>


    @GET("ssakti/users/friendrequest/getFriendRequest/{receiverId}")
    suspend fun getFriendRequests(
        @Path("receiverId") receiverId: Int,
        @Header("Authorization") bearerToken: String
    ):Response<FriendRequestResponse>



    @PUT("ssakti/users/friendrequest/approveFriendRequest/{friendRequestId}")
    suspend fun approveFriendRequest(
        @Path("friendRequestId") friendRequestId: Int,
        @Header("Authorization") bearerToken: String
    ): Response<CommonResponse>
    // ApiService.kt



    @PUT("ssakti/users/friendrequest/rejectFriendRequest/{friendRequestId}")
    suspend fun rejectFriendRequest(
        @Path("friendRequestId") friendRequestId: Int,
        @Header("Authorization") bearerToken: String
    ): Response<CommonResponse>


    @GET("ssakti/users/user/userAbout/{uuid}")
    suspend fun getUserProfile(
        @Path("uuid") uuid: String,
        @Header("Authorization") token: String
    ): Response<UserProfileResponse>



    @POST("ssakti/users/friendrequest/sendFriendRequest/{senderId}/{receiverId}")
    suspend fun sendFriendRequest(
        @Path("senderId") senderId: Int,
        @Path("receiverId") receiverId: Int,
        @Header("Authorization") token: String
    ): Response<CommonResponse>

    @DELETE("ssakti/users/user/deleteUser/{userId}")
    fun deleteUser(
        @Path("userId") userId: String,
        @Header("Authorization") token: String
    ): Call<CommonResponse>


    @GET("ssakti/users/event/getAllEvents/{userId}/{districtId}")
    suspend fun getEvents(
        @Path("userId") userId: Int,
        @Path("districtId") districtId: Int,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 5,
        @Header("Authorization") authorization: String? = null
    ): Response<EventResponse>

    @DELETE("ssakti/users/event/delete/{hostUserId}/{eventId}")
    suspend fun deleteEvent(
        @Path("hostUserId") hostUserId: Int,
        @Path("eventId") eventId: Int,
        @Header("Authorization") authorization: String? = null
    ): Response<DeleteResponse>

    @GET("ssakti/users/event/getDetails/{userId}/{eventUUID}")
    suspend fun getEventDetails(
        @Path("userId") userId: Int,
        @Path("eventUUID") eventUUID: String,
        @Header("Authorization") token: String?
    ): Response<EventDetailResponse>



    @POST("ssakti/users/participant/add/{eventId}")
    suspend fun joinEvent(
        @Header("Authorization") token: String,
        @Path("eventId") eventId: Int
    ): Response<JoinEventResponse>


    // 2️⃣ GET PARTICIPANTS

    @GET("ssakti/users/participant/getParticipants/{eventUUID}")
    suspend fun getParticipants(
        @Header("Authorization") token: String,
        @Path("eventUUID") eventUUID: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ParticipantResponse>


    @DELETE("ssakti/users/participant/exitEvent/{userId}/{eventId}")
    suspend fun exitEvent(
        @Header("Authorization") authToken: String,
        @Path("userId") userId: Int,
        @Path("eventId") eventId: Int
    ): Response<CommonResponse>

    @GET("ssakti/supadmin/eventcatg/get")
    suspend fun getEventCategories(
        @Header("Authorization") token: String
    ): Response<EventCategoryResponse>

    // response class for create API
    @Multipart
    @POST("ssakti/users/event/create/{hostId}/{districtId}")
    suspend fun createEvent(
        @Header("Authorization") token: String,
        @Path("hostId") hostId: Int,
        @Path("districtId") districtId: Int,
        @Query("eventCatgId") eventCatgId: Int,
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part eventImage: MultipartBody.Part? = null
    ): Response<CreateEventResponse>



    @GET("ssakti/users/event/getHostEvents/{hostUserId}")
    suspend fun getHostEvents(
        @Header("Authorization") authorization: String?,
        @Path("hostUserId") hostUserId: Int,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 5
    ): Response<EventResponse>



    //bookmar
    @GET("ssakti/users/savepost/getSavedPost/{userId}")
    suspend fun getSavedPosts(
        @Path("userId") userId: Int,
        @Query("size") size: Int,
        @Query("page") page: Int,
        @Header("Authorization") token: String
    ): Response<SavedPostResponse>



    @DELETE("ssakti/users/savepost/deletSavedPost/{userId}/{postId}")
    suspend fun deleteSavedPost(
        @Path("userId") userId: Long,
        @Path("postId") postId: Long,
        @Header("Authorization") token: String
    ): Response<CommonResponse>

    //page

    @GET("ssakti/users/pages/getAllPages/{userId}")
    suspend fun getAllPages(
        @Path("userId") userId: Int,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Header("Authorization") token: String
    ): Response<PageListResponse>




    @GET("ssakti/users/pages/getOwnPages/{userId}")
    suspend fun getOwnPages(
        @Path("userId") userId: Int,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Header("Authorization") token: String
    ): Response<PageListResponse>



    // FOLLOW page
    @POST("ssakti/users/pages/followPage/{userId}/{pagesId}")
    suspend fun followPage(
        @Path("userId") userId: Int,
        @Path("pagesId") pagesId: Int,
        @Header("Authorization") token: String
    ): Response<CommonResponse>


    // UNFOLLOW (EXIT page) — DELETE
    @DELETE("ssakti/users/pages/exitPage/{pageAdminUserId}/{pagesId}/{userId}")
    suspend fun unfollowPage(

        @Path("pageAdminUserId") pageAdminUserId: Int,
        @Path("pagesId") pagesId: Int,
        @Path("userId") userId: Int,
        @Header("Authorization") token: String
    ): Response<CommonResponse>


    @DELETE("ssakti/users/pages/deletePage/{pageAdminUserId}/{pagesId}")
    suspend fun deletePage(
        @Path("pageAdminUserId") pageAdminUserId: Int,
        @Path("pagesId") pagesId: Int,
        @Header("Authorization") token: String
    ): Response<CommonResponse>

    @POST("ssakti/users/pages/createPage")
    suspend fun createPage(
        @Query("adminUserId") adminUserId: Int,
        @Body body: CreatePageRequest,
        @Header("Authorization") token: String
    ): Response<CommonResponse>





    @GET("ssakti/users/pages/getPageDetails/{puuid}/{userId}")
    suspend fun getPageDetails(
        @Path("puuid") puuid: String,
        @Path("userId") userId: Int,
        @Query("page") page: Int,           // <<-- now a query param
        @Query("size") size: Int,
        @Header("Authorization") authorization: String // "Bearer <token>"
    ): Response<PageDetailsResponse>

    // Multipart variant — use only if server supports multipart for this endpoint


    @Multipart

    @POST("ssakti/users/pages/addPost/{pageAdminUserId}/{pageId}")
    suspend fun addPagePostMultipart(
        @Path("pageAdminUserId") pageAdminUserId: Int,
        @Path("pageId") pageId: Int,
        @Part("postName") postName: RequestBody,
        @Part("postType") postType: RequestBody,
        @Part("videoThumbnailUrl") videoThumbnailUrl: RequestBody,
        @Part("hashtag") hashtags: RequestBody?,
        @Part("mentionId") mentionIds: RequestBody?,
        @Part postImage: List<MultipartBody.Part>?,
        @Header("Authorization") authorization: String
    ): Response<CreatePagePostResponse>






    @Multipart
    @PUT("ssakti/users/pages/updatePage/{pageAdminUserId}/{pageId}")
    suspend fun updatePage(
        @Path("pageAdminUserId") pageAdminUserId: Int,
        @Path("pageId") pageId: Int,
        @Header("Authorization") authorization: String,   // Authorization: Bearer <jwt>
        @Header("token") token: String,                  // token: <raw-token>
        @Part("pageName") pageName: RequestBody,
        @Part("pageDescription") pageDescription: RequestBody,
        @Part("linkUrlName") linkUrlName: RequestBody,
        @Part("linkUrl") linkUrl: RequestBody,
        @Part coverImage: MultipartBody.Part? = null
    ): Response<CommonResponse>

    //notification
    @GET("ssakti/users/notification/getAllNotification/{userId}")
    suspend fun getAllNotifications(
        @Header("Authorization") token: String,
        @Path("userId") userId: Long
    ): Response<NotificationResponse>

    @POST("ssakti/users/notification/updateNotificationStatus/{receiverId}")
    suspend fun markAllNotificationsAsRead(
        @Header("Authorization") token: String,
        @Path("receiverId") receiverId: Long
    ): Response<MarkReadResponse>


    @GET("sskati/users/posts/getAds/ADMIN")
    suspend fun getAds(
        @Header("Authorization") authorization: String
    ): Response<AdsResponse>

    // prfile
    @GET("ssakti/users/user/getOrgDetails")
    suspend fun getOrgDetails(
        @Header("Authorization") authorization: String
    ): Response<com.collage.empowermentstrishakti.data.model.Profile.OrgDetailsResponse>

    @Multipart
    @PUT("ssakti/users/user/updateUser/{id}")
    suspend fun updateUserMultipart(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int,
        // text parts as RequestBody
        @Part("userDateOfBirth") userDateOfBirth: RequestBody?,
        @Part("userAddress") userAddress: RequestBody?,
        @Part("userFirstName") userFirstName: RequestBody?,
        @Part("userLastName") userLastName: RequestBody?,
        @Part("orgId") orgId: RequestBody?,
        @Part("subRole") subRole: RequestBody?,
        // file parts (may be null)
        @Part userProfileImagePath: MultipartBody.Part?,
        @Part userCoverProfileImagePath: MultipartBody.Part?
    ): Response<UpdateUserResponse>
    //group
    @GET("ssakti/users/group/getGroups/{userId}")
    suspend fun getGroups(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int
    ): Response<GroupListResponse>


    @GET("ssakti/users/group/getGroupDetails/{userId}/{groupUUID}")
    suspend fun getGroupDetails(
        @Path("userId") userId: Int,
        @Path("groupUUID") groupUUID: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Header("Authorization") token: String
    ): Response<GroupDetailsResponse>


    @Multipart
    @PUT("ssakti/users/group/updateGroup/{adminId}/{groupId}")
    suspend fun updateGroup(
        @Path("adminId") adminId: Int,
        @Path("groupId") groupId: Int,
        @Header("Authorization") token: String,
        @PartMap params: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part coverImage: MultipartBody.Part? = null
    ): Response<CommonResponse>




}



