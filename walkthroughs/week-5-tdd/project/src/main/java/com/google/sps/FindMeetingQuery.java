// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Arrays;
import java.util.ArrayList; 
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public final class FindMeetingQuery {
  /**
   * Given meeting information and all events on a certain day
   * Returns the times when the meeting can happen
   */ 
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<String> meetingAttendees = request.getAttendees();
    // No attendees return whole day time interval
    if (meetingAttendees.isEmpty()) return Arrays.asList(TimeRange.WHOLE_DAY);

    // Duration longer than a day return empty collection
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) return Arrays.asList();

    // Find time ranges that attendees in the meeting request are in
    ArrayList<TimeRange> conflictingTimes = new ArrayList<TimeRange>();
    getConflictingTimeRanges(conflictingTimes, events, meetingAttendees);

    // Prep  time ranges for finding open times
    Collections.sort(conflictingTimes, TimeRange.ORDER_BY_START);
    ArrayList<TimeRange> openTimes = new ArrayList<TimeRange>();
    Long goalDuration = request.getDuration();
    TimeRange currStart = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TimeRange.START_OF_DAY, false);
    
    // Iterate through each time range and find available times for meeting request
    for (int i = -1; i < conflictingTimes.size(); i++) {
      TimeRange nextRange;
      // Check edge case for next range if at last range
      if (i == conflictingTimes.size() - 1) { 
        nextRange = TimeRange.fromStartEnd(TimeRange.END_OF_DAY + 1, TimeRange.END_OF_DAY + 1, true);
      } else {
        nextRange = conflictingTimes.get(i + 1);
      }
      
      TimeRange currRange;
      // Check edge case for current range if at start
      if (i == -1) {
        currRange = currStart;
      } else {
        currRange = conflictingTimes.get(i);
      }
    
      // If no overlap can create potenital time range
      if (nextRange.overlaps(currRange)) {
        // If next range is not a subset of current range need to update current start
        if (!currRange.contains(nextRange)) {
          int newStart = nextRange.end();
          currStart = TimeRange.fromStartEnd(newStart, newStart, false);
        } 

      // No overlap between current and next range, can create potenital time range  
      } else {
        TimeRange potenitalTimeRange = TimeRange.fromStartEnd(currStart.start(), nextRange.start(), false);
        // Check if time range matches duration requirements
        if (potenitalTimeRange.duration() >= request.getDuration()) openTimes.add(potenitalTimeRange);
          int newStart = nextRange.end();
          currStart = TimeRange.fromStartEnd(newStart, newStart, false);
        }  
    }

    return openTimes;
  }
  
  /**
   * Check if any of meeting attendees are in the event
   */
  private Boolean attendeeInEvent(Collection<String> meetingAttendees, Set<String> eventAttendees) {
    // Iterate through meeting attendee check if attendee is in event      
    for (String attendee : meetingAttendees) {
      if (eventAttendees.contains(attendee)) return true;
    }
    return false;
  }
  
  /**
   * Find time ranges that attendees in the meeting request are in
   */
  private void getConflictingTimeRanges(ArrayList<TimeRange> conflictingTimes, Collection<Event> events, Collection<String> meetingAttendees) {
    for (Event event : events) {
      // Check if attendee in meeting request is in event
      if (attendeeInEvent(meetingAttendees, event.getAttendees())) {
        conflictingTimes.add(event.getWhen());
      }
    }
  }

}
