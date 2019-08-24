MAIN_CLASS=com.alteredmechanism.classversionextractor.ClassVersionExtractor
INCLUDES=-Isrc/main/java
OBJS=ClassVersionExtractor.o ClassVersion.o JavaFileFilter.o
CLASSES=build/classes/com/alteredmechanism/classversionextractor/*.class
SOURCES=src/main/java/com/alteredmechanism/classversionextractor/*.java

PROGRAM=classver

$(PROGRAM): all
	gcj -o $(PROGRAM) -Ibuild/classes --main=$(MAIN_CLASS) $(CLASSES)

all: $(SOURCES)
	gcj -C $(INCLUDES) -d build/classes $(SOURCES)

clean:
	rm -f classver $(CLASSES)
