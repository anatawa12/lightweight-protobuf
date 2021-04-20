package com.anatawa12.protobuf.benchmark

import com.anatawa12.protobuf.benchmark.google.First.SimpleData as GGSimpleData
import com.anatawa12.protobuf.benchmark.wire.SimpleData as WRSimpleData
import com.anatawa12.protobuf.benchmark.lightweight.SimpleData as LWSimpleData
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
open class SimpleStruct {
    companion object {
        const val count = 0x1000
    }

    @State(Scope.Thread)
    open class Input {
        @Setup(Level.Iteration)
        fun setUp() {
            val values = List(count) {
                createTriple(
                    doubleValue = random.getNextDouble(),
                    floatValue = random.getNextFloat(),
                    int64Value = random.nextLong(),
                    uint64Value = random.nextLong(),
                    fixed64Value = random.nextLong(),
                    sfixed64Value = random.nextLong(),
                    sint64Value = random.nextLong(),
                    int32Value = random.nextInt(),
                    fixed32Value = random.nextInt(),
                    sfixed32Value = random.nextInt(),
                    uint32Value = random.nextInt(),
                    sint32Value = random.nextInt(),
                    boolValue = random.nextBoolean(),
                    stringValue = random.nextString(),
                    bytesValue = random.nextBytes(),
                )
            }
            lw = values.map { it.first }.toTypedArray()
            gg = values.map { it.second }.toTypedArray()
            wr = values.map { it.third }.toTypedArray()
        }

        var random = Random(Random.nextLong())

        lateinit var lw: Array<LWSimpleData>
        lateinit var gg: Array<GGSimpleData>
        lateinit var wr: Array<WRSimpleData>
    }

    @Benchmark
    fun lw(hole: Blackhole, input: Input) {
        for (simpleData in input.lw) {
            val out = ByteArrayOutputStream()
            simpleData.writeTo(out)
            hole.consume(out)
        }
    }

    @Benchmark
    fun gg(hole: Blackhole, input: Input) {
        for (simpleData in input.lw) {
            val out = ByteArrayOutputStream()
            simpleData.writeTo(out)
            hole.consume(out)
        }
    }

    @Benchmark
    fun wr(hole: Blackhole, input: Input) {
        for (simpleData in input.lw) {
            val out = ByteArrayOutputStream()
            simpleData.writeTo(out)
            hole.consume(out)
        }
    }
}
