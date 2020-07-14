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
import java.util.Collection;
import java.util.HashSet;

public final class FindMeetingQuery {

  
  /**
   * Given meeting information and all events on a certain day
   * Returns the times when the meeting can happen
   */ 
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    // No attendees return whole day time interval
    if (request.getAttendees().isEmpty()) return Arrays.asList(TimeRange.WHOLE_DAY);

    // Duration longer than a day return empty collection
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) return Arrays.asList();

    /* Meeting: name, duration in minutes, collection of attendees */
    /* Event: name, time range, collection of attendees */
    /* time range: start time, end time, duration */
    // If attendees of a event not apart of the meeting it shouldn't effect meeting
    // One event split time zones into two options
    // Consider different events of attendees
    // Events that are subsets of another
    // No conflicts & all conflicts
    return Arrays.asList();
  }
}
