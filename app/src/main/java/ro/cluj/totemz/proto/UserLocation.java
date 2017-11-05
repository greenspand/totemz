// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: location.proto
package ro.cluj.totemz.proto;

import com.squareup.wire.FieldEncoding;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.ProtoReader;
import com.squareup.wire.ProtoWriter;
import com.squareup.wire.WireField;
import com.squareup.wire.internal.Internal;

import java.io.IOException;

import okio.ByteString;

public final class UserLocation extends Message<UserLocation, UserLocation.Builder> {
  public static final ProtoAdapter<UserLocation> ADAPTER = new ProtoAdapter_UserLocation();

  private static final long serialVersionUID = 0L;

  public static final String DEFAULT_CLIENTID = "";

  public static final Double DEFAULT_LATITUDE = 0.0d;

  public static final Double DEFAULT_LONGITUDE = 0.0d;

  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String clientID;

  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#DOUBLE"
  )
  public final Double latitude;

  @WireField(
      tag = 3,
      adapter = "com.squareup.wire.ProtoAdapter#DOUBLE"
  )
  public final Double longitude;

  public UserLocation(String clientID, Double latitude, Double longitude) {
    this(clientID, latitude, longitude, ByteString.EMPTY);
  }

  public UserLocation(String clientID, Double latitude, Double longitude, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.clientID = clientID;
    this.latitude = latitude;
    this.longitude = longitude;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.clientID = clientID;
    builder.latitude = latitude;
    builder.longitude = longitude;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof UserLocation)) return false;
    UserLocation o = (UserLocation) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(clientID, o.clientID)
        && Internal.equals(latitude, o.latitude)
        && Internal.equals(longitude, o.longitude);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (clientID != null ? clientID.hashCode() : 0);
      result = result * 37 + (latitude != null ? latitude.hashCode() : 0);
      result = result * 37 + (longitude != null ? longitude.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (clientID != null) builder.append(", clientID=").append(clientID);
    if (latitude != null) builder.append(", latitude=").append(latitude);
    if (longitude != null) builder.append(", longitude=").append(longitude);
    return builder.replace(0, 2, "UserLocation{").append('}').toString();
  }

  public static final class Builder extends Message.Builder<UserLocation, Builder> {
    public String clientID;

    public Double latitude;

    public Double longitude;

    public Builder() {
    }

    public Builder clientID(String clientID) {
      this.clientID = clientID;
      return this;
    }

    public Builder latitude(Double latitude) {
      this.latitude = latitude;
      return this;
    }

    public Builder longitude(Double longitude) {
      this.longitude = longitude;
      return this;
    }

    @Override
    public UserLocation build() {
      return new UserLocation(clientID, latitude, longitude, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_UserLocation extends ProtoAdapter<UserLocation> {
    public ProtoAdapter_UserLocation() {
      super(FieldEncoding.LENGTH_DELIMITED, UserLocation.class);
    }

    @Override
    public int encodedSize(UserLocation value) {
      return ProtoAdapter.STRING.encodedSizeWithTag(1, value.clientID)
          + ProtoAdapter.DOUBLE.encodedSizeWithTag(2, value.latitude)
          + ProtoAdapter.DOUBLE.encodedSizeWithTag(3, value.longitude)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, UserLocation value) throws IOException {
      ProtoAdapter.STRING.encodeWithTag(writer, 1, value.clientID);
      ProtoAdapter.DOUBLE.encodeWithTag(writer, 2, value.latitude);
      ProtoAdapter.DOUBLE.encodeWithTag(writer, 3, value.longitude);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public UserLocation decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.clientID(ProtoAdapter.STRING.decode(reader)); break;
          case 2: builder.latitude(ProtoAdapter.DOUBLE.decode(reader)); break;
          case 3: builder.longitude(ProtoAdapter.DOUBLE.decode(reader)); break;
          default: {
            FieldEncoding fieldEncoding = reader.peekFieldEncoding();
            Object value = fieldEncoding.rawProtoAdapter().decode(reader);
            builder.addUnknownField(tag, fieldEncoding, value);
          }
        }
      }
      reader.endMessage(token);
      return builder.build();
    }

    @Override
    public UserLocation redact(UserLocation value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
