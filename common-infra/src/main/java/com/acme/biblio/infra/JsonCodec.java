package com.acme.biblio.infra;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class JsonCodec {
  private static final ObjectMapper MAPPER = new ObjectMapper()
      .registerModule(new JavaTimeModule())
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  private JsonCodec(){}
  public static String toJson(Object o){
    try { return MAPPER.writeValueAsString(o); }
    catch (JsonProcessingException e) { throw new RuntimeException(e); }
  }
  public static <T> T fromJson(String json, Class<T> t){
    try { return MAPPER.readValue(json, t); }
    catch (Exception e) { throw new RuntimeException(e); }
  }
}
