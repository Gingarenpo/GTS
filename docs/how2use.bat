@echo off
copy /b how2use\*.md all.md
pandoc all.md -d how2use.yaml
del all.md