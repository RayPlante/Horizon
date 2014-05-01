# $Id: Makefile,v 1.1 1997/12/05 06:52:51 rplante Exp $
# 
# Default Horizon subpackage Makefile:  version 1.1
#
# Provide basic methods for building a Horizon sub-package.  The target 
# methods should be edited to specialize the build for a particular 
# sub-package.  This might be necessary if building of code in subdirectories
# should proceed in a particular order.  
#
# NOTE: This makefile requires the GNU version of make (often called gmake).  
#
# Available targets (intended for interactive use):
#
# Source Code Check-out:
#  pkgco       Force a check out from RCS all .java files in this package
#  treeco      Force a check out from RCS all .java files in this package 
#              and any sub-packages
# Compiling:
#  pkgbuild    Force a compile of this package
#  treebuild   Force a compile of this package and any sub-packages
#
# Installing:
#  pkginstall  copy the class files in this package to the Horizon classes tree
#  treeinstall copy the class files in this package and any sub-packages to the
#              Horizon classes tree
#
# Creating documentation
#  pkgdoc      create API documentation for this package
#  treedoc     create API documentation for this package and any sub-packages
#
# Miscellaneous:
#  clean       clean up this directory and any subdirectories by removing all
#              class and javadoc files
#  veryclean   clean up this directory and any subdirectories by removing all
#              java source files
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
      PARENTPATH = $(shell if (echo $(DEFPKGPATH) | grep / 2>&1 >/dev/null);  \
                           then echo $(DEFPKGPATH) | sed -e 's/\/[^\/]*$$//'; \
                           else echo ''; fi)
	 ADMDIR := $(HORIZONDIR)/adm
          SRCDIR = $(HORIZONDIR)/src
        CLASSDIR = $(HORIZONDIR)/classes
      JAVADOCDIR = $(HORIZONDIR)/javadoc
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
override SUBPKGCOMP := $(shell echo $(PWD) | sed -e 's/^.*\///')
ifeq ($(strip $(PARENTPATH)),)
override    PKGPATH := $(SUBPKGCOMP)
else
override    PKGPATH := $(PARENTPATH)/$(SUBPKGCOMP)
endif
override    PKGNAME := $(shell echo $(PKGPATH) | sed -e 's/\//./g')

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
	@echo "  pkgco       check out from RCS all .java files in this package"
	@echo "  treeco      check out from RCS all .java files in this" \
                            "package and any"
	@echo "              sub-packages"
	@echo "  pkgbuild    compile this package"
	@echo "  treebuild   compile this package and any sub-packages"
	@echo "  pkginstall  copy the class files in this package to the" \
                            "Horizon classes tree"
	@echo "  treeinstall copy the class files in this package and any" \
                            "sub-packages to the"
	@echo "              Horizon classes tree"
	@echo "  pkgdoc      create API documentation for this package"
	@echo "  treedoc     create API documentation for this package and" \
                            "any sub-packages"
	@echo "  clean       clean up this directory and any subdirectories" \
                            "by removing all"
	@echo "              class and javadoc files"
	@echo "  veryclean   clean up this directory and any subdirectories" \
                            "by removing all"
	@echo "              java source files"

test:
	@echo HORIZONDIR=$(HORIZONDIR)
	@echo STDCLASSPATH=$(STDCLASSPATH)
	@echo ADMDIR=$(ADMDIR)
	@echo MAKETEMPLATE=$(MAKETEMPLATE)

testenv:
	@echo HORIZONDIR=$(HORIZONDIR)
	@echo STDCLASSPATH=$(STDCLASSPATH)
	@echo ADMDIR=$(ADMDIR)
	@echo MAKETEMPLATE=$(MAKETEMPLATE)
	@for DIR in $(SUBDIRS); do           \
            echo $(MAKE) -C $$DIR testenv;   \
            $(MAKE) -C $$DIR testenv;        \
        done

###################################################################
#
# clean up this directory in preparation for a rebuild
#
clean: cleantree

veryclean: verycleantree

cleanhere:
	-rm -f *.class .PKGBUILT .TREEBUILT *.html

verycleanhere: cleanhere
	-rm -f *.java .PKGCO .TREECO

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
	@echo " "; echo Creating new Makefile for $(PKGPATH)/$*; echo " "
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
treeco: rmalltreeco pkgco .TREECO

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
	@for DIR in $(SUBDIRS); do              \
            echo $(MAKE) -C $$DIR .TREEBUILT;   \
            $(MAKE) -C $$DIR .TREEBUILT;        \
            if [ ! $$? -eq 0 ]; then exit 1; fi \
        done
	touch .TREEBUILT

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
# create API documentation.  Note that these targets are meant only 
# for updating the documentation for this portion of the Horizon 
# package.  These targets are not intended to be made when 
# documentation for the entire package is desired.
#

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
