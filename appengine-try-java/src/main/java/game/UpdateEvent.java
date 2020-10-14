package org.hirschhorn.ricochet.game;

public class UpdateEvent {
  private UpdateEventType eventType;
  private UpdateEventData eventData;
  private Integer currentVersion;
  
  public UpdateEvent(UpdateEventType eventType, UpdateEventData eventData, Integer currentVersion) {
    super();
    this.eventType = eventType;
    this.eventData = eventData;
    this.currentVersion = currentVersion;
  }

  public UpdateEventType getEventType() {
    return eventType;
  }
  
  public UpdateEventData getEventData() {
    return eventData;
  }
  
  public Integer getCurrentVersion(){
    return currentVersion;
  }
  
}
