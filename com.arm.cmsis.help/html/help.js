/*-----------------------------------------------------------------------------
Help.JS

JavaScript for Help System
-----------------------------------------------------------------------------*/

/*---------------------------------------------------------
Global Variables
---------------------------------------------------------*/
var noteImg        = "note.gif";
var expandImg      = "plus.gif";
var collapseImg    = "minus.gif";

// these variable are used in the meta header (code below) and in the copyright paragraph - see FixupTagClasses()
var auth = "Ltd. and others";
var copyr = "Copyright &copy; 2021, " + auth + ".  All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse Public License 2.0  which accompanies this distribution, and is available at https://www.eclipse.org/legal/epl-2.0/"

/*---------------------------------------------------------
The code below executes as soon as this .js file is loaded into a page
---------------------------------------------------------*/
try {
document.writeln('<meta http-equiv="content-type" content="text/html; charset=windows-1252">');
document.writeln('<meta name="Generator" content="Manual Publisher">');
document.writeln('<meta name="Author" content="' + auth + '">');
document.writeln('<meta name="Copyright" content="' + copyr + '">');
document.writeln('<link rel="stylesheet" type="text/css" href="help.css">');
}
catch (err) {
    alert(err.description + "\n\n" + "<meta> data not loaded into html <header>.");
}
finally {
  // continue;
}

/*---------------------------------------------------------
Event Handlers
---------------------------------------------------------*/
try {
window.onload		= HandleOnLoad;
//document.onclick	= HandleOnClick;
}
catch (err) {
    alert(err.description + "\n\n" + "windo.onload or document.onclick error.");
}
finally {
  // continue;
}

/*-----------------------------------------------------------------------------
HandleOnLoad

This function is invoked when the page is loaded.  It adds default tags,
images, and other constant stuff to the page.
-----------------------------------------------------------------------------*/
function HandleOnLoad ()
{
try {
  FixupTagClasses();
  // FixupExpand();
}
catch (err) {
  alert(err.description + "\n\n" + "HandleOnLoad error.");
}
finally {
  return;
}
}

/*-----------------------------------------------------------------------------
FixupTagClasses

This function replaces the inner text for various P tag classes
-----------------------------------------------------------------------------*/
function FixupTagClasses ()
{
var i;
var pTags;

try {
pTags = document.getElementsByTagName("P");

for (i = 0; i < pTags.length; i++)
  {
  if (pTags[i].className.toLowerCase() == "note")
    pTags[i].innerHTML = "<img src='" + noteImg + "'> Note";
  else if (pTags[i].className.toLowerCase() == "wh")
    pTags[i].innerHTML = "Where";
  else if (pTags[i].className.toLowerCase() == "res")
    pTags[i].innerHTML = "<img src='" + noteImg + "'> Restriction";
  else if (pTags[i].className.toLowerCase() == "revhist")
    pTags[i].innerHTML = "<b>Revision History</b>";
  else if (pTags[i].className.toLowerCase() == "copyright")
    pTags[i].innerHTML = copyr;
  }
}
catch (err) {
  alert(err.description + "\n\n" + "FixupTagClasses error.");
}
finally {
  return;
}
}

/*-----------------------------------------------------------------------------
HandleOnClick

This function figures out which a tag was clicked on and invokes a function
based on the ID of the anchor.
-----------------------------------------------------------------------------*/
function HandleOnClick ()
{
try {
    var e;
    var eID; 
    
    // Look for the nearest parent anchor starting where we clicked.
    e = window.event.srcElement;
    
    for (var i = 0; i < 5; i++)
      {
      if (e.tagName.toLowerCase() != "a" && e.parentElement != null)
        e = e.parentElement;
      }				
    
    eID = e.id.toLowerCase();
    
    if (eID == "expand")
      AnchorExpand (e);
}
catch (err) {
  alert(err.description + "\n\n" + "HandleOnClick error.");
}
finally {
  return;
}
}

/*-----------------------------------------------------------------------------
FixupExpand

This function replaces all anchors with the expand ID to include the proper
image and class definition.
-----------------------------------------------------------------------------*/
function FixupExpand ()
{
try {
var i;

for (i = 0; i < document.anchors.length; i++)
  {
  if (document.anchors[i].id.toLowerCase() == "expand")
    {
    document.anchors[i].title = "Expand/Collapse";
    document.anchors[i].innerHTML = "<img src='" + expandImg + "' class='expand'> " + document.anchors[i].innerHTML;
    }
  }
}
catch (err) {
  alert(err.description + "\n\n" + "FixupExpand error.");
}
finally {
  return;
}
}

/*-----------------------------------------------------------------------------
AnchorExpand

This function expands or collapses an expand div.
-----------------------------------------------------------------------------*/
function AnchorExpand (e)
{
try {
var oExp = GetExpandDiv(e);
var oImg = GetChildImage(e);

if (oExp.style.display.toLowerCase() == "block")
  BlockCollapse(oExp, oImg);
else
  BlockExpand(oExp, oImg);

event.returnValue = false;
}
catch (err) {
  alert(err.description + "\n\n" + "AnchorExpand error.");
}
finally {
  return;
}
}

/*-----------------------------------------------------------------------------
GetExpandDiv (element)

Finds the expand div for an expand anchor.
-----------------------------------------------------------------------------*/
function GetExpandDiv (element)
{
try {
var oE = element;
var iNextTag;
var oExpandableDiv;
var i;

for (i = 1; i < 5; i++)
  {
  iNextTag = oE.sourceIndex + oE.children.length + i;
  oExpandableDiv = document.all (iNextTag);
  if (oExpandableDiv.className.toLowerCase() == "expand" || iNextTag == document.all.length)
    break;
  }

return oExpandableDiv;
}
catch (err) {
  alert(err.description + "\n\n" + "GetExpandDiv error.");
}
finally {
  return;
}
}

/*-----------------------------------------------------------------------------
GetChildImage (element)

Finds the chip image of an expand anchor.
-----------------------------------------------------------------------------*/
function GetChildImage (element)
{
try {
var oE = element;

if (oE.tagname != "img")
  oE = oE.children.tags("img")(0);

return (oE);
}
catch (err) {
  alert(err.description + "\n\n" + "GetChildImage error.");
}
finally {
  return;
}
}

/*-----------------------------------------------------------------------------
BlockExpand

Expands blocks that were previously collapsed.
-----------------------------------------------------------------------------*/
function BlockExpand (oExpDiv, oImage)
{
try {
  oExpDiv.style.display = "block";
  oImage.src = collapseImg;
}
catch (err) {
  alert(err.description + "\n\n" + "BlockExpand error.");
}
finally {
  return;
}
}

/*-----------------------------------------------------------------------------
BlockCollapes

Collapses blocks that were previously expanded
-----------------------------------------------------------------------------*/
function BlockCollapse (oExpDiv, oImage)
{
try {
  oExpDiv.style.display = "none";
  oImage.src = expandImg;
}
catch (err) {
  alert(err.description + "\n\n" + "BlockCollapse error.");
}
finally {
  return;
}
}

/*-----------------------------------------------------------------------------
linkTo

Link to documents that reside outside of any *.chm book.
PARAM: fileName - can have relative path.
-----------------------------------------------------------------------------*/
function linkTo (fileName) 
{
try {
  var strStart, strEnd, delim, pos, link;
  delim = /:/;
  pos = location.href.search(delim);
  if (pos == 2) {					                   
      strStart = 14;
  }
  else {						                       
      strStart = 7;
  }

  delim = "\\";
  strEnd = location.href.lastIndexOf(delim) + 1;     
  link = location.href.substring(strStart, strEnd)
  link = 'file:///' + link + fileName;
  window.open(link);
}
catch (err) {
  alert(err.description + "\n\n" + "This document could not be found.");
}
finally {
  return;
}
}

/*-----------------------------------------------------------------------------
-----------------------------------------------------------------------------*/
