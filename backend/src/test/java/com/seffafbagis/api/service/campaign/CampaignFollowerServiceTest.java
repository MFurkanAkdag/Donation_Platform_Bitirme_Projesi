package com.seffafbagis.api.service.campaign;

import com.seffafbagis.api.dto.mapper.CampaignMapper;
import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.campaign.CampaignFollower;
import com.seffafbagis.api.entity.campaign.CampaignFollowerId;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.exception.ResourceNotFoundException;
import com.seffafbagis.api.repository.CampaignFollowerRepository;
import com.seffafbagis.api.repository.CampaignRepository;
import com.seffafbagis.api.repository.UserRepository;
import com.seffafbagis.api.security.SecurityUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignFollowerServiceTest {

    @Mock
    private CampaignFollowerRepository campaignFollowerRepository;
    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CampaignMapper campaignMapper;

    @InjectMocks
    private CampaignFollowerService followerService;

    @Nested
    @DisplayName("Follow Campaign Tests")
    class FollowCampaignTests {

        @Test
        @DisplayName("Should successfully follow a campaign")
        void followCampaign_Success() {
            UUID campaignId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            try (MockedStatic<SecurityUtils> utilities = Mockito.mockStatic(SecurityUtils.class)) {
                utilities.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(userId));
                when(campaignRepository.existsById(campaignId)).thenReturn(true);
                when(campaignFollowerRepository.existsById(any(CampaignFollowerId.class))).thenReturn(false);

                followerService.followCampaign(campaignId);

                verify(campaignFollowerRepository).save(any(CampaignFollower.class));
            }
        }

        @Test
        @DisplayName("Should not save when already following")
        void followCampaign_AlreadyFollowing() {
            UUID campaignId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            try (MockedStatic<SecurityUtils> utilities = Mockito.mockStatic(SecurityUtils.class)) {
                utilities.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(userId));
                when(campaignRepository.existsById(campaignId)).thenReturn(true);
                when(campaignFollowerRepository.existsById(any(CampaignFollowerId.class))).thenReturn(true);

                followerService.followCampaign(campaignId);

                verify(campaignFollowerRepository, never()).save(any(CampaignFollower.class));
            }
        }

        @Test
        @DisplayName("Should throw exception when campaign not found")
        void followCampaign_CampaignNotFound() {
            UUID campaignId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            try (MockedStatic<SecurityUtils> utilities = Mockito.mockStatic(SecurityUtils.class)) {
                utilities.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(userId));
                when(campaignRepository.existsById(campaignId)).thenReturn(false);

                assertThrows(ResourceNotFoundException.class, () -> followerService.followCampaign(campaignId));
            }
        }
    }

    @Nested
    @DisplayName("Unfollow Campaign Tests")
    class UnfollowCampaignTests {

        @Test
        @DisplayName("Should successfully unfollow a campaign")
        void unfollowCampaign_Success() {
            UUID campaignId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            try (MockedStatic<SecurityUtils> utilities = Mockito.mockStatic(SecurityUtils.class)) {
                utilities.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(userId));

                followerService.unfollowCampaign(campaignId);

                verify(campaignFollowerRepository).deleteById(any(CampaignFollowerId.class));
            }
        }
    }

    @Nested
    @DisplayName("Get Followers To Notify Tests")
    class GetFollowersToNotifyTests {

        @Test
        @DisplayName("Should return users to notify for updates")
        void getFollowersToNotify_ForUpdate_ReturnsCorrectUsers() {
            UUID campaignId = UUID.randomUUID();

            User user1 = new User();
            ReflectionTestUtils.setField(user1, "id", UUID.randomUUID());

            User user2 = new User();
            ReflectionTestUtils.setField(user2, "id", UUID.randomUUID());

            User user3 = new User();
            ReflectionTestUtils.setField(user3, "id", UUID.randomUUID());

            CampaignFollower follower1 = new CampaignFollower();
            follower1.setUser(user1);
            follower1.setNotifyOnUpdate(true);
            follower1.setNotifyOnComplete(true);

            CampaignFollower follower2 = new CampaignFollower();
            follower2.setUser(user2);
            follower2.setNotifyOnUpdate(true);
            follower2.setNotifyOnComplete(false);

            CampaignFollower follower3 = new CampaignFollower();
            follower3.setUser(user3);
            follower3.setNotifyOnUpdate(false);
            follower3.setNotifyOnComplete(true);

            when(campaignFollowerRepository.findByCampaignId(campaignId))
                    .thenReturn(Arrays.asList(follower1, follower2, follower3));

            List<UUID> userIds = followerService.getFollowersToNotify(campaignId, true);

            assertEquals(2, userIds.size());
            assertTrue(userIds.contains(user1.getId()));
            assertTrue(userIds.contains(user2.getId()));
            assertFalse(userIds.contains(user3.getId()));
        }

        @Test
        @DisplayName("Should return users to notify for completion")
        void getFollowersToNotify_ForComplete_ReturnsCorrectUsers() {
            UUID campaignId = UUID.randomUUID();

            User user1 = new User();
            ReflectionTestUtils.setField(user1, "id", UUID.randomUUID());

            User user2 = new User();
            ReflectionTestUtils.setField(user2, "id", UUID.randomUUID());

            CampaignFollower follower1 = new CampaignFollower();
            follower1.setUser(user1);
            follower1.setNotifyOnUpdate(true);
            follower1.setNotifyOnComplete(true);

            CampaignFollower follower2 = new CampaignFollower();
            follower2.setUser(user2);
            follower2.setNotifyOnUpdate(true);
            follower2.setNotifyOnComplete(false);

            when(campaignFollowerRepository.findByCampaignId(campaignId))
                    .thenReturn(Arrays.asList(follower1, follower2));

            List<UUID> userIds = followerService.getFollowersToNotify(campaignId, false);

            assertEquals(1, userIds.size());
            assertTrue(userIds.contains(user1.getId()));
            assertFalse(userIds.contains(user2.getId()));
        }

        @Test
        @DisplayName("Should return empty list when no followers")
        void getFollowersToNotify_NoFollowers_ReturnsEmptyList() {
            UUID campaignId = UUID.randomUUID();

            when(campaignFollowerRepository.findByCampaignId(campaignId))
                    .thenReturn(List.of());

            List<UUID> userIds = followerService.getFollowersToNotify(campaignId, true);

            assertTrue(userIds.isEmpty());
        }
    }

    @Nested
    @DisplayName("Is Following Tests")
    class IsFollowingTests {

        @Test
        @DisplayName("Should return true when user is following")
        void isFollowing_WhenFollowing_ReturnsTrue() {
            UUID campaignId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            try (MockedStatic<SecurityUtils> utilities = Mockito.mockStatic(SecurityUtils.class)) {
                utilities.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(userId));
                when(campaignFollowerRepository.existsById(any(CampaignFollowerId.class))).thenReturn(true);

                boolean result = followerService.isFollowing(campaignId);

                assertTrue(result);
            }
        }

        @Test
        @DisplayName("Should return false when user is not following")
        void isFollowing_WhenNotFollowing_ReturnsFalse() {
            UUID campaignId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            try (MockedStatic<SecurityUtils> utilities = Mockito.mockStatic(SecurityUtils.class)) {
                utilities.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(userId));
                when(campaignFollowerRepository.existsById(any(CampaignFollowerId.class))).thenReturn(false);

                boolean result = followerService.isFollowing(campaignId);

                assertFalse(result);
            }
        }

        @Test
        @DisplayName("Should return false when user is not logged in")
        void isFollowing_WhenNotLoggedIn_ReturnsFalse() {
            UUID campaignId = UUID.randomUUID();

            try (MockedStatic<SecurityUtils> utilities = Mockito.mockStatic(SecurityUtils.class)) {
                utilities.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.empty());

                boolean result = followerService.isFollowing(campaignId);

                assertFalse(result);
            }
        }
    }
}
