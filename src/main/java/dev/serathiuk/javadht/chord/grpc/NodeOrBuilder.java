// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: chord.proto

// Protobuf Java Version: 3.25.3
package dev.serathiuk.javadht.chord.grpc;

public interface NodeOrBuilder extends
    // @@protoc_insertion_point(interface_extends:Node)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>uint32 id = 1;</code>
   * @return The id.
   */
  int getId();

  /**
   * <code>string host = 2;</code>
   * @return The host.
   */
  java.lang.String getHost();
  /**
   * <code>string host = 2;</code>
   * @return The bytes for host.
   */
  com.google.protobuf.ByteString
      getHostBytes();

  /**
   * <code>uint32 port = 3;</code>
   * @return The port.
   */
  int getPort();
}
