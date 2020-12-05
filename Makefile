MAIN_CLASS=com.alteredmechanism.classversionextractor.ClassVersionExtractor
SRC_DIR=src/main/java
INCLUDES=-I$(SRC_DIR)
OBJS=ClassVersionExtractor.o ClassVersion.o JavaFileFilter.o
CLASSES=build/classes/com/alteredmechanism/classversionextractor/*.class
SOURCES=src/main/java/com/alteredmechanism/classversionextractor/*.java
LINUX_DISTRO:=$(shell . /etc/os-release && echo $$ID)
ifeq ($(LINUX_DISTRO),void)
	GCJ=gcj-6
else
	GCJ=gcj
endif

PROGRAM=classver

$(PROGRAM): all
	$(GCJ) -o $(PROGRAM) -Ibuild/classes --main=$(MAIN_CLASS) $(CLASSES)

all: $(SOURCES)
	#time ecj -d build/classes $(SOURCES)
	$(GCJ) -C $(INCLUDES) -d build/classes $(SOURCES)

install: $(PROGRAM)
	install -s $(PROGRAM) /usr/local/bin

clean:
	rm -f classver $(CLASSES)
