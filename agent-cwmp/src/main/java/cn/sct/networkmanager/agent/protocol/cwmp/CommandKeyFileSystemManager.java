package cn.sct.networkmanager.agent.protocol.cwmp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class CommandKeyFileSystemManager {
    private static final Logger log = LoggerFactory.getLogger(CommandKeyFileSystemManager.class);
    private final RandomAccessFile raf;
    private final FileChannel channel;
    private static final long maxSize=64 * 1024;
    private final ReentrantLock lock = new ReentrantLock();

    // 文件头信息
    private static final int HEADER_SIZE = 32;
    private static final int WRITE_POSITION_OFFSET = 0;
    private static final int READ_POSITION_OFFSET = 8;
    private static final int ELEMENT_COUNT_OFFSET = 16;
    private static final int FIRST_ELEMENT_OFFSET = 24;
    private static final int ELEMENT_SIZE=4;

    public CommandKeyFileSystemManager() throws IOException {
        this.raf = new RandomAccessFile("/cwmp/data/commandKey.record", "rw");
        this.channel = raf.getChannel();
        if (raf.length() == 0) {
            raf.setLength(maxSize);
        }
        initializeHeader();
    }

    private void initializeHeader() throws IOException {
            try {
                lock.lock();
                ByteBuffer header = ByteBuffer.allocate(HEADER_SIZE);
                channel.position(0);
                int bytesRead = channel.read(header);
                if (bytesRead == 0 || header.getInt(WRITE_POSITION_OFFSET) == 0) {
                    header.clear();
                    header.putLong(WRITE_POSITION_OFFSET, HEADER_SIZE);
                    header.putLong(READ_POSITION_OFFSET, HEADER_SIZE);
                    header.putLong(ELEMENT_COUNT_OFFSET, 0);
                    header.putLong(FIRST_ELEMENT_OFFSET, HEADER_SIZE);
                    header.flip();
                    channel.position(0);
                    channel.write(header);
                }
            } finally {
              ;lock.unlock();
            }
    }

    public long add(byte[] data) throws IOException {
        try{
                lock.lock();
                HeaderInfo header = readHeader();
                int elementSize = ELEMENT_SIZE + data.length;
                long writePos = header.writePosition;
                if (writePos + elementSize > maxSize){//清理数据，从头写入
                    clear();
                    header=readHeader();
                }
                ByteBuffer dataBuffer = ByteBuffer.allocate(4);
                dataBuffer.putInt(data.length);
                dataBuffer.put(data);
                dataBuffer.flip();
                channel.position(writePos);
                channel.write(dataBuffer);
                long newWritePos = writePos + elementSize;
                header.writePosition = newWritePos;
                header.elementCount++;
                writeHeader(header);
                return writePos;
        }finally {
            lock.unlock();
        }

    }

    /**
     * 根据索引随机读取元素
     */
    public byte[] get(long index) throws IOException {
        try {
            lock.lock();
            if (index < 0) {
                throw new IndexOutOfBoundsException("索引无效");
            }
            HeaderInfo header = readHeader();
            if (header.elementCount == 0) {
                return null;
            }
            if (index > header.elementCount){
                throw new RuntimeException("数组越界错误");
            }
            // 从读取位置开始查找指定索引的元素
            long currentPos = header.readPosition;
            long currentIndex = 0;
            while (currentIndex <= index) {
                // 读取长度
                ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
                channel.position(currentPos);
                channel.read(lengthBuffer);
                lengthBuffer.flip();
                int length = lengthBuffer.getInt();
                if (currentIndex == index) {
                    byte[] data = new byte[length];
                    ByteBuffer dataBuffer = ByteBuffer.wrap(data);
                    channel.read(dataBuffer);
                    return data;
                }
                currentPos += 4 + length;
                currentIndex++;
            }
            return null; // 未找到
        } finally {
            lock.unlock();
        }
    }

    /**
     * 读取文件头信息
     */
    private HeaderInfo readHeader() throws IOException {
        ByteBuffer headerBuffer = ByteBuffer.allocate(HEADER_SIZE);
        channel.position(0);
        channel.read(headerBuffer);
        headerBuffer.flip();
        HeaderInfo header = new HeaderInfo();
        header.writePosition=headerBuffer.getInt(WRITE_POSITION_OFFSET);
        header.readPosition=headerBuffer.getInt(READ_POSITION_OFFSET);
        header.elementCount = headerBuffer.getLong(ELEMENT_COUNT_OFFSET);
        header.firstElementPosition = headerBuffer.getLong(FIRST_ELEMENT_OFFSET);
        headerBuffer.clear();
        return header;
    }
    /**
     * 写入文件头信息
     */
    private void writeHeader(HeaderInfo header) throws IOException {
        ByteBuffer headerBuffer = ByteBuffer.allocate(HEADER_SIZE);
        headerBuffer.putLong(WRITE_POSITION_OFFSET, header.writePosition);
        headerBuffer.putLong(READ_POSITION_OFFSET, header.readPosition);
        headerBuffer.putLong(ELEMENT_COUNT_OFFSET, header.elementCount);
        headerBuffer.putLong(FIRST_ELEMENT_OFFSET, header.firstElementPosition);
        headerBuffer.flip();
        channel.position(0);
        channel.write(headerBuffer);
    }



    public long size() throws IOException {
        try {
            lock.lock();
            HeaderInfo header = readHeader();
            return header.elementCount;
        } finally {
          lock.unlock();
        }
    }


    public void clear() throws IOException {
          try {
              lock.lock();
              HeaderInfo header = new HeaderInfo();
              header.writePosition = HEADER_SIZE;
              header.readPosition = HEADER_SIZE;
              header.elementCount = 0;
              header.firstElementPosition = HEADER_SIZE;
              writeHeader(header);
          } finally {
            lock.unlock();
          }
    }


    public void close()  {
        try{
            channel.close();
            raf.close();
        }catch (IOException e){
            log.warn("关闭文件失败");
        }

    }


    private static class HeaderInfo {
        long writePosition;
        long readPosition;
        long elementCount;
        long firstElementPosition;
    }

}
