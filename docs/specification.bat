@echo off
copy /b specification\*.md all.md
pandoc all.md -d specification.yaml
del all.md