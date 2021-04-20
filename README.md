lightweight protobuf
=====

A lightweight protocol buffer implementation for java.

## Why not [Google's official protobuf for java][google-protobuf] nor [wire by Square][square-wire]?

Because those are a little heavy, I made this.

[protobuf-javalite-3.15.8.jar] is 694 kilo-bytes and uses reflection.

[wire-runtime-3.7.0.jar] is 189 kilo-bytes, uses reflection, and needs 
[okio-2.8.0.jar], 243 kilo-bytes jar so in total 432 kilo-bytes.

I want to make protobuf implementation in less than 100 kilo-bytes.

## Status

- [x] implement data struct compiler
- [x] implement parser
- [x] implement writer

[google-protobuf]: https://github.com/protocolbuffers/protobuf/
[square-wire]: https://github.com/square/wire/
[protobuf-javalite-3.15.8.jar]: https://repo1.maven.org/maven2/com/google/protobuf/protobuf-javalite/3.15.8/protobuf-javalite-3.15.8.jar
[wire-runtime-3.7.0.jar]: https://repo1.maven.org/maven2/com/squareup/wire/wire-runtime/3.7.0/wire-runtime-3.7.0.jar
[okio-2.8.0.jar]: https://repo1.maven.org/maven2/com/squareup/okio/okio/2.8.0/okio-2.8.0.jar
