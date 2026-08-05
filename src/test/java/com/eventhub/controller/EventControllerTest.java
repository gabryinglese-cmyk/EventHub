package com.eventhub.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.eventhub.domain.dto.CreateEventRequest;
import com.eventhub.domain.dto.EventDto;
import com.eventhub.domain.dto.PageResponse;
import com.eventhub.domain.dto.UserDto;
import com.eventhub.exception.ResourceNotFoundException;
import com.eventhub.security.JwtAuthenticationFilter;
import com.eventhub.security.JwtProvider;
import com.eventhub.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(EventController.class)
@AutoConfigureMockMvc(addFilters = false)
class EventControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private EventService eventService;

        @MockitoBean
        private JwtProvider jwtProvider;

        @MockitoBean
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        private UUID eventId;
        private UUID userId;

        private UserDto userDto;

        private EventDto eventDto;

        private CreateEventRequest createEventRequest;

        @BeforeEach
        void setUp() {

                eventId = UUID.randomUUID();

                userId = UUID.randomUUID();

                userDto = new UserDto(
                                userId,
                                "test@example.com",
                                "John",
                                "Doe",
                                LocalDateTime.now(),
                                LocalDateTime.now());

                eventDto = new EventDto(
                                eventId,
                                "Test Event",
                                "Description",
                                LocalDateTime.now().plusDays(1),
                                "Torino",
                                100,
                                userDto,
                                LocalDateTime.now(),
                                LocalDateTime.now());

                createEventRequest = new CreateEventRequest(
                                "Test Event",
                                "Description",
                                LocalDateTime.now().plusDays(1),
                                "Torino",
                                100);

        }

        private PageResponse<EventDto> pageResponse() {

                return new PageResponse<>(
                                List.of(eventDto),
                                0,
                                10,
                                1,
                                1,
                                true,
                                true,
                                false,
                                false);
        }

        @Test
        @WithMockUser
        void testCreateEvent() throws Exception {

                when(eventService.createEvent(any(), any()))
                                .thenReturn(eventDto);

                mockMvc.perform(
                                post("/events")
                                                .with(csrf())
                                                .param("userId", userId.toString())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(createEventRequest)))
                                .andExpect(status().isCreated());

                verify(eventService)
                                .createEvent(any(), any());

        }

        @Test
        @WithMockUser
        void testGetEventById() throws Exception {

                when(eventService.getEventById(eventId))
                                .thenReturn(eventDto);

                mockMvc.perform(
                                get("/events/" + eventId))
                                .andExpect(status().isOk());

                verify(eventService)
                                .getEventById(eventId);

        }

        @Test
        @WithMockUser
        void testGetAllEvents() throws Exception {

                when(eventService.getAllEvents(any()))
                                .thenReturn(pageResponse());

                mockMvc.perform(
                                get("/events"))
                                .andExpect(status().isOk());

        }

        @Test
        @WithMockUser
        void testGetUpcomingEvents() throws Exception {

                when(eventService.getUpcomingEvents(
                                any(),
                                any(),
                                any()))
                                .thenReturn(pageResponse());

                mockMvc.perform(
                                get("/events/upcoming")
                                                .param("startDateTime",
                                                                LocalDateTime.now().toString())
                                                .param("endDateTime",
                                                                LocalDateTime.now().plusDays(10).toString()))
                                .andExpect(status().isOk());

        }

        @Test
        @WithMockUser
        void testGetEventsByUser() throws Exception {

                when(eventService.getEventsByUser(
                                any(),
                                any()))
                                .thenReturn(pageResponse());

                mockMvc.perform(
                                get("/events/user/" + userId))
                                .andExpect(status().isOk());

        }

        @Test
        @WithMockUser
        void testSearchEvents() throws Exception {

                when(eventService.searchEvents(
                                any(),
                                any()))
                                .thenReturn(pageResponse());

                mockMvc.perform(
                                get("/events/search")
                                                .param("searchTerm", "java"))
                                .andExpect(status().isOk());

        }

        @Test
        @WithMockUser
        void testFilterEvents() throws Exception {

                when(eventService.filterEvents(
                                any(),
                                any(),
                                any(),
                                any(),
                                any(),
                                any()))
                                .thenReturn(pageResponse());

                mockMvc.perform(
                                get("/events/filter")
                                                .param("location", "Torino"))
                                .andExpect(status().isOk());

        }

        @Test
        @WithMockUser
        void testUpdateEvent() throws Exception {

                when(eventService.updateEvent(any(), any()))
                                .thenReturn(eventDto);

                mockMvc.perform(
                                put("/events/" + eventId)
                                                .with(csrf())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(eventDto)))
                                .andExpect(status().isOk());

        }

        @Test
        @WithMockUser
        void testDeleteEvent() throws Exception {

                doNothing()
                                .when(eventService)
                                .deleteEvent(eventId);

                mockMvc.perform(
                                delete("/events/" + eventId)
                                                .with(csrf()))
                                .andExpect(status().isNoContent());

                verify(eventService)
                                .deleteEvent(eventId);

        }

        @Test
        @WithMockUser
        void testCreateEvent_InvalidRequest() throws Exception {

                CreateEventRequest invalidRequest = new CreateEventRequest(
                                "",
                                "",
                                null,
                                "",
                                0);

                mockMvc.perform(
                                post("/events")
                                                .with(csrf())
                                                .param("userId", userId.toString())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest());

        }

        @Test
        @WithMockUser
        void testGetEventById_NotFound() throws Exception {

                when(eventService.getEventById(eventId))
                                .thenThrow(new ResourceNotFoundException(
                                                "Event not found"));

                mockMvc.perform(
                                get("/events/" + eventId))
                                .andExpect(status().isNotFound());

        }

}