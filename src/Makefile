# $Id: Makefile,v 1.3 1998/02/03 10:03:33 rplante Exp $
# 
# Master Makefile
#
# Provides basic methods for building the entire Horizon package.  
#
# NOTE: This makefile requires the GNU version of make (often called gmake).  
#
# Available targets (intended for interactive use):
# Basic targets:
#  build       build the entire Horizon package
#  install     install the built Horizon package by copying all class files
#              to the classes tree
#  javadoc     create API docs for entire Horizon package 
#  Horizon     build, install and create API docs for the entire Horizon 
#              package 
#
# Miscellaneous:
#  clean       clean up this directory and any subdirectories by removing all
#              class and javadoc files
#  veryclean   clean up this directory and any subdirectories by removing all
#              java source files
#  treeco      Force a check out from RCS all .java files in this package 
#              and any sub-packages
#  treebuild   Force a compile of this package and any sub-packages
#  treeinstall copy the class files in this package and any sub-packages to the
#              Horizon classes tree
#  treedoc     create API documentation for this package and any sub-packages
#
# The building of sub-packages can be restricted to certain directories by
# specifying the SUBDIRS variable, e.g. to build only certain subdirectories:
#
#     gmake SUBDIRS="systems converters" treebuild
#
#
##############################################################################
#
# CONFIGURATION:
override PWD := $(shell pwd)
#
# These should normally be set by the calling program

ifeq ($(strip $(HORIZONDIR)),)
export HORIZONDIR = /afs/ncsa/projects/horizon/Horizon
endif

ifeq ($(strip $(STDCLASSPATH)),)
export STDCLASSPATH = /usr/java/lib:/usr/java/classes:/usr/java/lib/classes.zip
endif

# These may or may not be set already
     DEFPKGPATH := $(shell echo $(PWD) | sed -e 's/^.*\/src\///' \
                                             -e 's/^.*\/devlp\///')
      PARENTPATH = $(shell echo $(DEFPKGPATH) | sed -e 's/\/[^\/]*$$//')
	  ADMDIR = $(HORIZONDIR)/adm
          SRCDIR = $(HORIZONDIR)/src
        CLASSDIR = $(HORIZONDIR)/classes
      JAVADOCDIR = $(HORIZONDIR)/docs/api
       CLASSPATH = $(SRCDIR):$(STDCLASSPATH):.
          JDEBUG = 
           JOPTS = $(JDEBUG) -classpath $(CLASSPATH)
          TMPDIR = /tmp
     HORIZONPATH = ncsa/horizon

    MAKETEMPLATE = $(ADMDIR)/Makefile.default
      MAKECIOPTS = -t-"Default Makefile" -m"initial version"

             REV = 
ifeq ($(strip $(REV)),)
           COREV = 
else
           COREV = -r$REV
endif
          COOPTS = $(COREV)


# These should not be altered
# override SUBPKGCOMP := $(shell echo $(PWD) | sed -e 's/^.*\///')
# override    PKGPATH := $(PARENTPATH)/$(SUBPKGCOMP)
# override    PKGNAME := $(shell echo $(PKGPATH) | sed -e 's/\//./g')

        PACKAGES = $(shell find $(SRCDIR) -type d \! \( -name RCS -prune \) \
                           \! -name .\* -print | sed -e 's%^$(SRCDIR)%%'    \
                           -e 's/^\///' -e 's/\//./g' | grep -v             \
                           '^ncsa$$' | grep -v '^ncsa.horizon$$')

         SUBDIRS = `find . -type d \! -name RCS \! -name .\* -print \
                    -prune | sed -e 's/^\.\///'`
override ALLSRCS := $(shell find RCS -name \*.java,v -print | \
                            sed -e 's/^RCS\///' -e 's/,v$$//')
            SRCS = $(ALLSRCS)
        MAKEDIRS = $(SUBDIRS)
        SUBMAKES = $(shell for DIR in $(SUBDIRS); do echo $$DIR/Makefile; done)

#
###################################################################
#
# have the default target be to print out help information
#
help:
	@echo "Available make targets:"
	@echo "  build       build the entire Horizon package"
	@echo "  install     install the built Horizon package by copying" \
                            "all class files"
	@echo "              to the classes tree"
	@echo "  javadoc     create API docs for entire Horizon package"
	@echo "  Horizon     build, install and create API docs for the" \
                            "entire Horizon package" 
	@echo "  clean       clean up this directory and any subdirectories" \
                            "by removing all"
	@echo "              class and javadoc files"
	@echo "  veryclean   clean up this directory and any subdirectories" \
                            "by removing all"
	@echo "              java source files"
	@echo " "
	@echo "  treeco      check out from RCS all .java files in this" \
                            "package and any"
	@echo "              sub-packages"
	@echo "  treebuild   compile this package and any sub-packages"
	@echo "  treeinstall copy the class files in this package and any" \
                            "sub-packages to the"
	@echo "              Horizon classes tree"

testenv:
	@echo HORIZONDIR=$(HORIZONDIR)
	@echo STDCLASSPATH=$(STDCLASSPATH)
	@echo ADMDIR=$(ADMDIR)
	@for DIR in $(SUBDIRS); do           \
            echo $(MAKE) -C $$DIR testenv;   \
            $(MAKE) -C $$DIR testenv;        \
        done

test:
	@echo $(PACKAGES)

###################################################################
#
# basic building targets for entire Horizon package
#

build: .TREECO .TREEBUILT

install: treeinstall clean

Horizon: build install javadoc

#
###################################################################
#
# clean up this directory in preparation for a rebuild
#
clean: cleantree

veryclean: verycleantree

cleanhere:
	-rm -f *.class .PKGBUILT .TREEBUILT *.html

verycleanhere: cleanhere
	-rm -f *.java .PKGCO

cleantree: cleanhere
	@for DIR in $(SUBDIRS); do           \
            echo $(MAKE) -C $$DIR cleantree; \
            $(MAKE) -C $$DIR cleantree;      \
        done

verycleantree: verycleanhere
	@for DIR in $(SUBDIRS); do               \
            echo $(MAKE) -C $$DIR verycleantree; \
            $(MAKE) -C $$DIR verycleantree;      \
        done
	-rm -f .TREECO

#
###################################################################
#
# make sure all subdirectories have Makefiles.  If there is one,
# check it out from RCS; otherwise, copy over a default version
# and check it into RCS.
#
.PRECIOUS: %/RCS/Makefile,v %.java .PKGBUILT .PKGCO .TREECO .TREEBUILT

%/Makefile: %/RCS/Makefile,v
	co $*/Makefile

%/RCS/Makefile,v:
	@if [ -f $*/Makefile ]; then                              \
            echo " ";                                             \
            echo Warning: $*/Makefile should be checked into RCS; \
            echo " ";                                             \
            exit 0;                                               \
        fi
	@echo " "; echo Creating new Makefile for $*; echo " "
	cp $(MAKETEMPLATE) $*/Makefile
	ci $(MAKECIOPTS) $*/Makefile
	@if [ ! -f $@ ]; then                                     \
            echo Failed to create default Makefile for $*;        \
            exit 1;                                               \
        fi;

# check in this Makefile
makeci:
	@if [ -f RCS/Makefile,v ]; then     \
            echo ci Makefile;               \
            ci Makefile;                    \
        else                                \
            echo ci $(MAKECIOPTS) Makefile; \
            ci $(MAKECIOPTS) Makefile;      \
        fi

# check out Makefiles for sub directories
submakes: $(SUBMAKES)

# create a new Makefile for a subdirectory
newmake:  $(SUBMAKES)
ifneq ($(strip $(MAKEDIRS)),)
	@for DIR in $(MAKEDIRS); do             \
            echo $(MAKE) -C $$DIR newmake;      \
            $(MAKE) -C $$DIR newmake;           \
            if [ ! $$? -eq 0 ]; then exit 1; fi \
        done
endif

#
###################################################################
#
# check out files from RCS
#
# check out a source file
%.java: 
	co $(COOPTS) $@

# check out everything in the package
pkgco: rmpkgco rmtreeco .PKGCO

rmpkgco:
	if [ -f .PKGCO ]; then rm .PKGCO; fi

.PKGCO:
ifneq ($(strip $(SRCS)),)
	co $(COOPTS) $(SRCS)
endif
	touch .PKGCO

# check out everything in this package, including subdirectories
treeco: pkgco .TREECO

rmtreeco:
	if [ -f .TREECO ]; then rm .TREECO; fi

rmalltreeco:
	find . -name .TREECO -exec rm -f '{}' \;

.TREECO: $(SUBMAKES) .PKGCO
	@for DIR in $(SUBDIRS); do              \
            echo $(MAKE) -C $$DIR .TREECO;      \
            $(MAKE) -C $$DIR .TREECO;           \
            if [ ! $$? -eq 0 ]; then exit 1; fi \
        done
	touch .TREECO

#
###################################################################
#
# Compile Java source files
#
# compile a file: Note this may not work unless other needed source code
#   is checked out or the CLASSPATH is altered from its default
%.class: %.java
	javac $(JOPTS) $*

# compile this package
pkgbuild: rmpkgbuilt rmtreebuilt .PKGBUILT

rmpkgbuilt:
	if [ -f .PKGBUILT ]; then rm .PKGBUILT; fi

rmtreebuilt:
	if [ -f .TREEBUILT ]; then rm .TREEBUILT; fi

rmalltreebuilt:
	find . -name .TREEBUILT -exec rm -f '{}' \;

.PKGBUILT: .PKGCO
	@if [ `ls *.java 2> /dev/null | wc -l` -gt 0 ]; then \
            echo javac $(JOPTS) \*.java;      \
            javac $(JOPTS) *.java;            \
        fi
	touch .PKGBUILT	

# compile this tree
treebuild: $(SUBMAKES) rmalltreebuilt .TREECO treecompile

# compile this tree without checking out source code
treecompile: pkgbuild .TREEBUILT

.TREEBUILT: .TREECO .PKGBUILT
	$(MAKE) -C Acme .TREEBUILT
	$(MAKE) -C FITSWCS .TREEBUILT
	$(MAKE) -C ncsa .TREEBUILT
	$(MAKE) -C apps .TREEBUILT
	@for DIR in $(SUBDIRS); do                  \
            if [ ! -f $$DIR/.PKGBUILT ]; then       \
                echo $(MAKE) -C $$DIR .TREEBUILT;   \
                $(MAKE) -C $$DIR .TREEBUILT;        \
                if [ ! $$? -eq 0 ]; then exit 1; fi \
            fi;                                     \
        done

#
###################################################################
#
# install Java class files into the proper place in the class tree
#
# copy classes to classes tree
pkginstall: .PKGBUILT
	@if [ ! -d $(CLASSDIR) ]; then  \
            echo $(CLASSDIR) not found; \
            exit 1;                     \
        fi
	@if [ ! -d $(CLASSDIR)/$(PKGPATH) ]; then \
            mkdir -p $(CLASSDIR)/$(PKGPATH);      \
        fi
ifneq ($(strip $(shell ls *.class 2> /dev/null)),)
	cp *.class $(CLASSDIR)/$(PKGPATH)
endif

# copy classes in this tree to classes tree
treeinstall: pkginstall
	@for DIR in $(SUBDIRS); do              \
            echo $(MAKE) -C $$DIR treeinstall;  \
            $(MAKE) -C $$DIR treeinstall;       \
            if [ ! $$? -eq 0 ]; then exit 1; fi \
        done

#
###################################################################
#
# create API documentation.  

cleanjavadoc:
	-rm -f $(JAVADOCDIR)/*.html

javadoc: cleanjavadoc
	javadoc -classpath $(CLASSPATH) -d $(JAVADOCDIR) $(PACKAGES)

# Note that the following targets are meant only 
# for updating the documentation for this portion of the Horizon 
# package.  Instead use the "javadoc" target to generate 
# documentation for the entire package.

pkgdoc: .PKGCO
ifneq ($(strip $(shell ls *.java 2> /dev/null)),)
	javadoc -classpath $(CLASSPATH) *.java
endif

treedoc: .TREECO pkgdoc
	@for DIR in $(SUBDIRS); do              \
            echo $(MAKE) -C $$DIR treedoc;      \
            $(MAKE) -C $$DIR treedoc;           \
            if [ ! $$? -eq 0 ]; then exit 1; fi \
        done

pkginstdoc: 
	cp $(PKGNAME).*.html $(JAVADOCDIR)
	-rm -f $(PKGNAME).*.html AllNames.html packages.html tree.html

treeinstdoc: pkginstdoc
	@for DIR in $(SUBDIRS); do              \
            echo $(MAKE) -C $$DIR treeinstdoc;  \
            $(MAKE) -C $$DIR treeinstdoc;       \
            if [ ! $$? -eq 0 ]; then exit 1; fi \
        done

#
###################################################################
