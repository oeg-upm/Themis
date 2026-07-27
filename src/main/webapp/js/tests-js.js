function escapeHTML(str) {
    if (str === null || str === undefined) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function escapeRegExp(str) {
    if (!str) return '';
    return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

var LOADER_HTML = '<div class="loader"><span></span><span></span><span></span></div>';

function showLoader(selector) {
    $(selector).html(LOADER_HTML).prop('disabled', true);
}

function hideLoader(selector, text) {
    $(selector).html(text).prop('disabled', false);
}

function setActionButtonsEnabled(enabled) {
    $('#checktests, #export, #clean, #loadtest, #loadtestfile').prop('disabled', !enabled);
}

function showNotification(message, type) {
    type = type || 'danger';

    var cleanMsg = message;
    if (typeof message === 'string' && message.trim().toLowerCase().startsWith('<!doctype')) {
        cleanMsg = 'Server error. Please check the provided data or URL.';
    } else {
        cleanMsg = escapeHTML(message);
    }

    var $alert = $('#app-notification');
    if (!$alert.length) {
        $alert = $('<div id="app-notification" class="alert alert-dismissible" role="alert" style="position: fixed; top: 70px; right: 20px; z-index: 9999; max-width: 450px; box-shadow: 0 4px 10px rgba(0,0,0,0.15); display: none;">' +
            '<button type="button" class="close" onclick="$(\'#app-notification\').fadeOut();" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
            '<span class="notification-body"></span>' +
            '</div>');
        $('body').append($alert);
    }

    $alert.removeClass('alert-danger alert-warning alert-info alert-success').addClass('alert-' + type);
    $alert.find('.notification-body').html(cleanMsg);
    $alert.stop(true, true).fadeIn();

    setTimeout(function() {
        $alert.fadeOut();
    }, 5000);
}

$(document).ready( function () {

    if ($('#header-placeholder').length) {
        var isSubfolder = window.location.pathname.includes('/conformance/') || window.location.pathname.includes('/testing/');
        var navbarPath = isSubfolder ? '../components/navbar.html' : 'components/navbar.html';
        $('#header-placeholder').load(navbarPath, function() {
            var page = window.location.pathname.split('/').pop() || 'index.html';
            $('#navbar .nav a').each(function() {
                var href = $(this).attr('href');
                if (href === page || (page === '' && href === 'index.html')) {
                    $(this).parent().addClass('active');
                } else {
                    $(this).parent().removeClass('active');
                }
            });
            if (isSubfolder) {
                $('#navbar a[href]').each(function() {
                    var href = $(this).attr('href');
                    if (!href.startsWith('http')) {
                        $(this).attr('href', '../' + href);
                    }
                });
                $('#navbar img[src]').each(function() {
                    var src = $(this).attr('src');
                    if (!src.startsWith('http')) {
                        $(this).attr('src', '../' + src);
                    }
                });
            }
        });
    }

    if ($('#footer-placeholder').length) {
        var isSubfolder = window.location.pathname.includes('/conformance/') || window.location.pathname.includes('/testing/');
        var footerPath = isSubfolder ? '../components/footer.html' : 'components/footer.html';
        $('#footer-placeholder').load(footerPath, function() {
            if (isSubfolder) {
                $('#footer-placeholder img[src]').each(function() {
                    var src = $(this).attr('src');
                    if (!src.startsWith('http')) {
                        $(this).attr('src', '../' + src);
                    }
                });
            }
        });
    }

    $.ajax({
        type: 'GET',
        dataType: "json",
        url: 'rest/api/renewsession',
        success: function (data, textStatus, jqXHR) {
        },
        error: function (ts) {
        }
    });
});


$( function() {



    function split( val ) {
        return val.split( / \s*/ );
    }
    function extractLast( term ) {
        return split( term ).pop();
    }
    function checkURI( term ) {
        if(term[1] != null)
            return term[1].value;
        else
            return "";
    }

    function checkFile( term ) {
        if(term[0] != null)
            return term[0].value.replace(/><\/http.*:>/g,'\/>');
        else
            return "";
    }


    $( "#test" )
        .keydown( function( event ) {
            if ( event.keyCode === $.ui.keyCode.TAB &&
                $( this ).autocomplete( "instance" ).menu.active ) {
                event.preventDefault();
            }
            syntaxChecker();
        })
        .keypress( function( event ) {
            if ( event.keyCode === $.ui.keyCode.TAB &&
                $( this ).autocomplete( "instance" ).menu.active ) {
                event.preventDefault();
            }
        })
        .autocomplete({
            minLength: 1,
            source: function( request, response ) {
                var tables =document.getElementsByName('tablegot');

                var myObj ={};
                tables.forEach(function(table) {

                    var tableid= table.id;
                    //loops through rows
                    var myRows = [];
                    var $headers = $(table).find("th");
                    var $rows = $(table).find("tbody tr").each(function (index) {
                        $cells = $(this).find("td.got");

                        myRows[index] = {};
                        $cells.each(function (cellIndex) {
                            var  header = $($headers[cellIndex]).html();
                            header = header.replace("<span class=\"glyphicon glyphicon-pencil\"></span>","");
                            myRows[index][header] = $(this).html();
                        });
                    });

                    myObj[table.id] = myRows;
                });
                $.ajax({
                    type: "POST",
                    url:"rest/api/autocompleteFromUriFile",
                    contentType: "application/json",
                    data: JSON.stringify({
                        test: request.term,
                        lastTerm: extractLast( request.term ),
                        ontologyUri: checkURI(document.getElementsByName("ontology")),
                        code: checkFile(document.getElementsByName("ontologycode")),
                    }),
                    success: response,
                    dataType: 'json'
                });

            },
            focus: function() {
                // prevent value inserted on focus
                return false;
            },
            select: function( event, ui ) {
                var terms = split( this.value );
                // remove the current input
                terms.pop();
                // add the selected item
                terms.push( ui.item.value );
                // add placeholder to get the comma-and-space at the end
                if(ui.item.value == ";"){
                    terms.push(",");
                }
                terms.push( "" );
                this.value = terms.join( " " );
                syntaxChecker();
                return false;
            }
        });
} );

function syntaxChecker(){
    var test = $('#test').val();
    $.ajax({
        type: 'GET',
        dataType: "json",
        data: { test: test},
        url: 'rest/api/syntaxChecker',
        success: function (data, textStatus, jqXHR) {
            var result = data;

            if(result == true){
                $("#test").css('background-color', 'rgba(204, 255, 204, 0.3)');
            }else{
                $("#test").css('background-color', 'rgba(255, 102, 102, 0.3)');
            }
        },
        error: function (data, textStatus, jqXHR) {
        }
    });

}

// ascending order
function sortByLabel(x,y) {
    return x.Label - y.Label;
}

function sortJSON(jsonArray) {
    return jsonArray.sort(sortByLabel);
}


function hideRows(text) {
    index = 1;
    tr = document.getElementById('tr'+index);
    while (tr!=null){
        valores = document.getElementById('inp'+index).value;
        if (valores.indexOf(text)==-1){
            tr.style.display='none';
        }
        index++;
        tr = document.getElementById('tr'+index);
    }
    document.getElementById('remButt').style.display='';
}


function removeCheck(id) {
    $('#notmatch').html('');
    $('#'+id).remove();
    if($('#storedtests').html().trim().length == 0) {
        $('#checksuite').attr("disabled", true)

    }
}

function removeAll(id) {

    var labels = document.getElementsByName("testlabel");
    var report = document.getElementsByName("report");
    report.forEach(function (item) {
        item.style.visibility = 'hidden';
    });

    var len = labels.length;
    var parentNode = labels[0].parentNode;
    for(var i=0; i<len; i++)
    {
        parentNode.removeChild(labels[0]);
    }

    var remove = document.getElementById("removeall");
    remove.remove();
    $('#checksuite').attr("disabled", true);

}

function check() {
    showLoader('#checktests');
    $('#notmatch').html('');

    var tables =document.getElementsByName('tablegot');

    var myObj ={};
    tables.forEach(function(table) {

        var tableid= table.id;
        //loops through rows
        var myRows = [];
        var $headers = $(table).find("th");
        var $rows = $(table).find("tbody tr").each(function (index) {
            var $cells = $(this).find("td.got");

            myRows[index] = {};
            $cells.each(function (cellIndex) {
                var  header = $($headers[cellIndex]).html();
                header = header.replace("<span class=\"glyphicon glyphicon-pencil\"></span>","");
                myRows[index][header] = $(this).html();
            });
        });

        myObj[table.id] = myRows;
    });

    var array = $(this).serializeArray();
    var tests = document.getElementsByName("test")[0].value.split(";");

    var arrayontosId = [];
    var idontos = document.getElementsByName("ontology");
    idontos.forEach(function (item) {
        if(item.value!="") {
            arrayontosId.push(item.value);
        }
    });

    var arrayontosCode = [];
    var arrayontosCodeTextArea = document.getElementsByName("ontologycode");
    arrayontosCodeTextArea.forEach(function (item) {
        if(item.value!="") {
            var value =item.value;
            var ontologyCode = value.replace(/><\/http.*:>/g,'\/>');
            arrayontosCode.push(ontologyCode);
        }
    });

    var data = {
        got: JSON.stringify(myObj),
        ontologies:arrayontosId,
        ontologiesCode:arrayontosCode,
        tests: tests
    };

    $.ajax({
        type: 'POST',
        data: JSON.stringify(data),
        dataType: "json",
        url: 'rest/api/results',
        contentType: "application/json",
        success: function (data, textStatus, jqXHR) {
            if (data.length > 0) {
                $.each(data, function (i, item) {
                    var table = document.getElementById("table");
                    if(item.Results.length <= 1 && table != null){
                        $.each(item.Results, function (i, result) {
                            var table = document.getElementById("table");
                            var row = table.insertRow(1);
                            var cell1 = row.insertCell(0);
                            var cell2 = row.insertCell(1);
                            var cell3 = row.insertCell(2);
                            var cell4 = row.insertCell(3);
                            cell4.innerHTML = "<button type=\"button\" class=\"btn btn-default\" onclick=\"removeOntology(this)\" title=\"Remove test\" aria-label=\"Remove test\"> <span class=\"submit glyphicon glyphicon-trash\"></span> </button>";
                            if (result.Result == 'Passed') {
                                cell1.innerHTML = "<p name=\"testintable\">" + escapeHTML(item.Test) + "</p>";
                                cell2.innerHTML = "<span class=\"label label-success\" data-toggle=\"tooltip\" title=\"The ontology passed the test\">Passed</span>";
                                cell3.innerHTML = "<p>None</p>";
                            } else if (result.Result == 'Undefined') {
                                var test = escapeHTML(item.Test);

                                $.each(result.Undefined, function (j, undefinedTerm) {
                                    let safeTerm = escapeHTML(undefinedTerm);
                                    let re = new RegExp(`\\b${escapeRegExp(safeTerm)}\\b`, 'gi');
                                    test = test.replace(re, "<span style=\"color:red;\">"+safeTerm+"</span>");
                                });

                                cell1.innerHTML = "<p name=\"testintable\">" + test + "</p>";

                                cell2.innerHTML = "<span class=\"label label-default \" data-toggle=\"tooltip\" title=\"The ontology did not pass the test\">Undefined terms</span>";
                                cell3.innerHTML = "<p>The terms in the test are not defined in the ontology</p>";

                            }else if(result.Result == 'Incorrect'){
                                var test = escapeHTML(item.Test);
                                $.each(result.Incorrect, function (j, incorrectTerm) {
                                    let safeTerm = escapeHTML(incorrectTerm);
                                    let re = new RegExp(`\\b${escapeRegExp(safeTerm)}\\b`, 'gi');

                                    test = test.replace(re, "<span style=\"color:#ff7f50;\"> " + safeTerm + "</span>");
                                });
                                cell1.innerHTML = "<p name=\"testintable\">" + test + "</p>";

                                cell2.innerHTML = "<span class=\"label label-default \" data-toggle=\"tooltip\" title=\"The ontology did not pass the test\">Undefined terms</span>";
                                cell3.innerHTML = "<p>The terms in the test are not correctly defined in the ontology</p>";
                            } else if (result.Result == 'Absent') {
                                cell1.innerHTML = "<p name=\"testintable\">" + escapeHTML(item.Test) + "</p>";

                                cell2.innerHTML = "<span class=\"label label-warning\" data-toggle=\"tooltip\" title=\"The ontology did not pass the test\">Absent relation</span>";
                                cell3.innerHTML = "<p>The ontology does not implement the requirement associated to the test</p>";
                            } else {
                                cell1.innerHTML = "<p name=\"testintable\">" + escapeHTML(item.Test) + "</p>";

                                cell2.innerHTML = "<span class=\"label label-danger\" data-toggle=\"tooltip\" title=\"The ontology did not pass the test\">Conflict</span>";
                                cell3.innerHTML = "<p>The ontology has a relation which causes a conflict with the one define in the test</p>";
                            }
                        });
                    }else if(item.Results.length <= 1 && table == null) {

                        var table = document.getElementById("tablemultiple");
                        document.getElementsByName("tableresults")[0].setAttribute("id","table");
                        var table = document.getElementById("table");

                        $("#table").html("");

                        var header="<thead>\n"+
                            "\t<tr>\n" +
                            "\t\t<th>Test</th>\n"+
                            "\t\t<th>Result</th>\n" +
                            "\t\t<th>Problem</th>\n"+
                            "<th></th>\n"+
                            "\t</tr>\n"+
                            "</thead>";

                        $("#table").append(header);


                        $.each(item.Results, function (i, result) {
                            var row = table.insertRow(1);
                            var cell1 = row.insertCell(0);
                            var cell2 = row.insertCell(1);
                            var cell3 = row.insertCell(2);
                            var cell4 = row.insertCell(3);
                            cell1.innerHTML = "<p name=\"testintable\">" + escapeHTML(item.Test) + "</p>";
                            cell4.innerHTML = "<button type=\"button\" class=\"btn btn-default\" onclick=\"removeOntology(this)\" title=\"Remove test\" aria-label=\"Remove test\"> <span class=\"submit glyphicon glyphicon-trash\"></span> </button>";
                            if (result.Result == 'Passed') {
                                cell2.innerHTML = "<span class=\"label label-success\" data-toggle=\"tooltip\" title=\"The ontology passed the test\">Passed</span>";
                                cell3.innerHTML = "<p>None</p>";
                            } else if (result.Result == 'Undefined') {
                                var test = escapeHTML(item.Test);
                                $.each(result.Undefined, function (j, undefinedTerm) {
                                    let safeTerm = escapeHTML(undefinedTerm);
                                    let re = new RegExp(`\\b${escapeRegExp(safeTerm)}\\b`, 'gi');
                                    test = test.replace(re, "<span style=\"color:red;\">"+safeTerm+"</span>");
                                });
                                cell1.innerHTML = "<p name=\"testintable\">" + test + "</p>";
                                cell2.innerHTML = "<span class=\"label label-default \" data-toggle=\"tooltip\" title=\"The ontology did not pass the test\">Undefined terms</span>";
                                cell3.innerHTML = "<p>The terms in the test are not defined in the ontology</p>";
                            } else if (result.Result == 'Incorrect') {
                                var test = escapeHTML(item.Test);
                                $.each(result.Incorrect, function (j, incorrectTerm) {
                                    let safeTerm = escapeHTML(incorrectTerm);
                                    let re = new RegExp(`\\b${escapeRegExp(safeTerm)}\\b`, 'gi');
                                    test = test.replace(re, "<span style=\"color:red;\">"+safeTerm+"</span>");
                                });
                                cell1.innerHTML = "<p name=\"testintable\">" + test + "</p>";
                                cell2.innerHTML = "<span class=\"label label-default \" data-toggle=\"tooltip\" title=\"The ontology did not pass the test\">Undefined terms</span>";
                                cell3.innerHTML = "<p>The terms in the test are not correctly defined in the ontology</p>";
                            } else if (result.Result == 'Absent') {
                                cell2.innerHTML = "<span class=\"label label-warning\" data-toggle=\"tooltip\" title=\"The ontology did not pass the test\">Absent relation</span>";
                                cell3.innerHTML = "<p>The ontology does not implement the requirement associated to the test</p>";
                            } else {
                                cell2.innerHTML = "<span class=\"label label-danger\" data-toggle=\"tooltip\" title=\"The ontology did not pass the test\">Conflict</span>";
                                cell3.innerHTML = "<p>The ontology has a relation which causes a conflict with the one define in the test</p>";
                            }
                        });

                    }else if(item.Results.length > 1 && table != null){

                        document.getElementById("table").setAttribute("id","tablemultiple");
                        var table = document.getElementById("tablemultiple");
                        $("#tablemultiple").html("");

                        var header="<thead>\n"+
                            "\t<tr>\n" +
                            "\t\t<th>Test</th>\n";

                        item.Results.forEach(function (ontology) {
                            header+="<th>"+escapeHTML(ontology.Ontology)+"</th>\n";
                        });
                        header+="<th></th>\n";
                        header+="\t</tr>\n"+
                            "</thead>";

                        $("#tablemultiple").append(header);


                        var tableStr = "<tbody>";
                        tableStr +="<td class=\"col\">"+escapeHTML(item.Test)+"</td>";
                        item.Results.forEach(function (result) {
                            if (result.Result == 'Passed') {
                                tableStr +="<td class=\"col\"><span class=\"label label-success\" data-toggle=\"tooltip\" title=\"The ontology passed the test\">Passed</span></td>";
                            } else if (result.Result == 'Undefined') {
                                tableStr += "<td class=\"col\"><span class=\"label label-default \" data-toggle=\"tooltip\" title=\""+escapeHTML(result.Undefined)+" not defined in the ontology\">Undefined terms</span></td>";
                            }else if (result.Result == 'Incorrect') {
                                tableStr += "<td class=\"col\"><span class=\"label label-default \" data-toggle=\"tooltip\" title=\""+escapeHTML(result.Incorrect)+" not correctly defined in the ontology \">Incorrect terms</span></td>";
                            } else if (result.Result == 'Absent') {
                                tableStr += "<td class=\"col\"><span class=\"label label-warning\" data-toggle=\"tooltip\" title=\"The ontology did not pass the test\">Absent relation</span></td>";
                            } else {
                                tableStr += "<td class=\"col\"><span class=\"label label-danger\" data-toggle=\"tooltip\" title=\"The ontology did not pass the test\">Conflict</span></td>";
                            }
                        });
                        tableStr +="<td class=\"col\"><button type=\"button\" class=\"btn btn-default\" onclick=\"removeOntology(this)\" title=\"Remove test\" aria-label=\"Remove test\"> <span class=\"submit glyphicon glyphicon-trash\"></span> </button></td></tbody>";
                        $("#tablemultiple").append(tableStr);
                    }else{

                        var table = document.getElementById("tablemultiple");

                        var row = table.insertRow(1);
                        var cell2 = row.insertCell(0);
                        cell2.innerHTML = "<td class=\"col\">"+escapeHTML(item.Test)+"</td>";
                        var index=1;
                        $.each(item.Results, function (i, result) {
                            var cell1 = row.insertCell(i+1);
                            if (result.Result == 'Passed') {
                                cell1.innerHTML ="<td class=\"col\"><span class=\"label label-success\" data-toggle=\"tooltip\" title=\"The ontology passed the test\">Passed</span></td>";
                            } else if (result.Result == 'Undefined') {
                                cell1.innerHTML ="<td class=\"col\"><span class=\"label label-default \" data-toggle=\"tooltip\" title=\""+escapeHTML(result.Undefined)+" not defined in the ontology\">Undefined terms</span></td>";
                            }else if (result.Result == 'Incorrect') {
                                cell1.innerHTML ="<td class=\"col\"><span class=\"label label-default \" data-toggle=\"tooltip\" title=\""+escapeHTML(result.Incorrect)+" not correctly defined in the ontology \">Incorrect terms</span></td>";
                            } else if (result.Result == 'Absent') {
                                cell1.innerHTML ="<td class=\"col\"><span class=\"label label-warning\" data-toggle=\"tooltip\" title=\"The ontology did not pass the test\">Absent relation</span></td>";
                            } else {
                                cell1.innerHTML = "<td class=\"col\"><span class=\"label label-danger\" data-toggle=\"tooltip\" title=\"The ontology did not pass the test\">Conflict</span></td>";
                            }
                            index++;


                        });
                        var cell3 = row.insertCell(index);
                        cell3.innerHTML = "<td class=\"col\"></td><button type=\"button\" class=\"btn btn-default\" onclick=\"removeOntology(this)\" title=\"Remove test\" aria-label=\"Remove test\"> <span class=\"submit glyphicon glyphicon-trash\"></span> </button></td>";


                    }
                });
            } else {

                $('#notmatch').html("<font color=\"red\">This is not a test expression supported by Themis</font>");

            }
            hideLoader('#checktests', 'Check');
        },
        error: function (data, textStatus, jqXHR) {
            hideLoader('#checktests', 'Check');

            $('#notmatch').html("<font color=\"red\">Something went wrong. Check that the ontology doesn't have any inconsistencies or unsatisfiable classes</font>");

        }
    });

}

function hover(but) {  //Inicia la edición de una fila
    var color = $(but).css('background-color');
    if(color == "rgb(220, 220, 220)" )
        $(but).css('background-color', "white");
    else
        $(but).css('background-color', "gainsboro");
}

function rowEdit(but) {  //Inicia la edición de una fila
    var $row = $(but).parents('tr');  //accede a la fila
    $row.find("td:nth-child(1)").each(function(){
        $(this).attr("contentEditable","true");
        var html = $(this).html();
        var input = $('<input type="text" />');
        input.val(html);
        $(this).html(input);
    });

    $row.find('td:nth-child(5)').css("display","block");
}

function rowSave(but) {  //Inicia la edición de una fila
    var html;
    var $row = $(but).parents('tr');  //accede a la fila
    $row.find("td:nth-child(1) input").each(function(){
        html = $(this).val();
        $(this).remove();
    });

    $row.find("td:nth-child(1)").each(function () {
        $(this).html(html);
    });


    var $row = $(but).parents('tr');  //accede a la fila
    $row.find('td:nth-child(1)').attr("contentEditable","false");
}

function loadontologyFromURI() {
    var array = $(this).serializeArray();
    var id = document.getElementsByName("ontology");
    id.forEach(function (item) {
        var test = document.getElementsByName(item.value);
        if(item.value!="" && test.length == 0 ) {
            array.push(item.value);

        }
    });

    array.forEach(function (uri) {
        showLoader('#load');
        var exists = document.getElementsByName(uri);
        if(uri!="" && exists.length ==0) {
            $.ajax({
                type: 'POST',
                dataType: "json",
                data: JSON.stringify(uri),
                url: 'rest/api/gotAsTableFromURI',
                success: function (data, textStatus, jqXHR) {
                    //  var text = "<p   name=\"" + uri + "\"><a href=\"" + uri + "\"><button  type=\"button\" class=\"btn btn-link\" ><small><span class=\"glyphicon glyphicon-ok align-middle\" aria-hidden=\"true\"></span></small></button></a></p>";
                    // $("#loadcheck").append(text);

                    var safeUri = escapeHTML(uri);
                    var safeKey = escapeHTML(data.key);

                    var text = "<p name=\"" + safeUri + "\"><a href=\"" + safeUri + "\" target=\"_blank\" rel=\"noopener noreferrer\"><button data-toggle=\"collapse\" name=\"ontology\" type=\"button\" class=\"btn btn-link\" value='" + safeUri + "'><small><span class=\"glyphicon glyphicon-ok align-middle\" aria-hidden=\"true\"></span> " + safeUri + "</small></button></a></p>";
                    $("#loadonto").append(text);


                    var text = "<p class=\"collapse-title\" name=\"" + safeUri + "\"><a><button data-toggle=\"collapse\" type=\"button\" data-target=\"#" + safeKey + "collapse\" aria-expanded=\"false\" aria-controls=\"" + safeKey + "collapse\" class=\"btn btn-link\" id=\"" + safeUri + "\" onclick='hover(this)'><span title=\"See got \" class=\"glyphicon glyphicon-chevron-down align-middle\" aria-hidden=\"true\"></span><small> See the glossary of terms</small> </button></a></p>";
                    $("#loadgot").append(text);

                    var text2 = "           <div name=\"" + safeUri + "\" style=\"background-color: gainsboro\" class=\"collapse col-md-12\" id=\"" + safeKey + "collapse\">\n" +
                        "                            <div class=\"card card-body\">\n" + data.got +

                        "                            </div>\n" +
                        "                        </div>";


                    $("#aux").append(text2);

                    var text = "<p name=\"" + safeUri + "\"><a><button type=\"button\" class=\"btn btn-link\" onclick=\"removeGot('" + safeUri.replace(/'/g, "\\'") + "')\" aria-label=\"Remove ontology\"><small><span title=\"Remove ontology\" class=\"glyphicon glyphicon-trash align-middle\" aria-hidden=\"true\"></span> Remove</small> </button></a></p>";
                    $("#deleteonto").append(text);



                    document.getElementById("ontology").value ="";
                    hideLoader('#load', 'Load from URI');
                    setActionButtonsEnabled(true);
                    var testsch = document.getElementById("checkout-tests");
                    testsch.style.opacity=1;


                },
                error: function (xhr) {
                    showNotification(xhr.responseText, 'danger');
                    document.getElementById("ontology").value ="";
                    hideLoader('#load', 'Load from URI');
                    setActionButtonsEnabled(true);
                    var testsch = document.getElementById("checkout-tests");
                    testsch.style.opacity=1;
                }
            });




        }



    });


}



function loadontologyFromFile(){
    var array = $(this).serializeArray();
    var id = document.getElementsByName("ontologyfile");
    id.forEach(function (item) {
        var test = document.getElementsByName(item.value);
        if(item.value!="" && test.length == 0 ) {
            array.push(item.value);

        }
    });


    array.forEach(function (ontologyfile) {
        showLoader('#loadfile');
        var exists = document.getElementsByName(ontologyfile);
        var ontologyCode = ontologyfile.replace(/><\/http.*:>/g,'\/>');
        if(ontologyfile!="" && exists.length ==0) {
            $.ajax({
                type: 'POST',
                dataType: "json",
                data: ontologyCode,
                url: 'rest/api/gotAsTableFromFile',
                success: function (data, textStatus, jqXHR) {

                    //  var text = "<p   name=\"" + uri + "\"><a href=\"" + uri + "\"><button  type=\"button\" class=\"btn btn-link\" ><small><span class=\"glyphicon glyphicon-ok align-middle\" aria-hidden=\"true\"></span></small></button></a></p>";
                    // $("#loadcheck").append(text);
                    var safeUri = escapeHTML(data.uri);
                    var safeKey = escapeHTML(data.key);
                    var safeCode = escapeHTML(ontologyfile);

                    var text = "<p name=\"" + safeUri + "\"><button data-toggle=\"collapse\" name=\"ontologyfile\" type=\"button\" class=\"btn btn-link\" value='" + safeUri + "'><small><span class=\"glyphicon glyphicon-ok align-middle\" aria-hidden=\"true\"></span> " + safeUri + "</small></button></p>";

                    text+="<div name=\"" + safeUri + "\"><textarea style=\"display:none;\" name=\"ontologycode\" id=\""+safeKey+"\">"+safeCode+"</textarea></div>";

                    $("#loadonto").append(text);

                    var text = "<p class=\"collapse-title\" name=\"" + safeUri + "\"><a><button data-toggle=\"collapse\" type=\"button\" data-target=\"#" + safeKey + "collapse\" aria-expanded=\"false\" aria-controls=\"" + safeKey + "collapse\" class=\"btn btn-link\" id=\"" + safeUri + "\" onclick='hover(this)'><span title=\"See got \" class=\"glyphicon glyphicon-chevron-down align-middle\" aria-hidden=\"true\"></span><small> See the glossary of terms</small> </button></a></p>";
                    $("#loadgot").append(text);

                    var text2 = "           <div name=\"" + safeUri + "\" style=\"background-color: gainsboro\" class=\"collapse col-md-12\" id=\"" + safeKey + "collapse\">\n" +
                        "                            <div class=\"card card-body\">\n" + data.got +

                        "                            </div>\n" +
                        "                        </div>";


                    $("#aux").append(text2);

                    var text = "<p name=\"" + safeUri + "\"><a><button type=\"button\" class=\"btn btn-link\" onclick=\"removeGot('" + safeUri.replace(/'/g, "\\'") + "')\" aria-label=\"Remove ontology\"><small><span title=\"Remove ontology\" class=\"glyphicon glyphicon-trash align-middle\" aria-hidden=\"true\"></span> Remove</small> </button></a></p>";
                    $("#deleteonto").append(text);


                    document.getElementById("ontologyfile").value ="";
                    hideLoader('#loadfile', 'Load from code');
                    setActionButtonsEnabled(true);
                    var testsch = document.getElementById("checkout-tests");
                    testsch.style.opacity=1;


                },
                error: function (xhr) {
                    showNotification(xhr.responseText, 'danger');
                    document.getElementById("ontologyfile").value ="";
                    hideLoader('#loadfile', 'Load from code');
                    setActionButtonsEnabled(true);
                    var testsch = document.getElementById("checkout-tests");
                    testsch.style.opacity=1;
                }
            });




        }



    });
}

function openTests() {
    window.open('tests-info.html', '_blank');
}


function removeGot(uri) {
    var elements = document.getElementsByName(uri);
    while(elements.length > 0){
        elements[0].parentNode.removeChild(elements[0]);
    }
}

function loadTests() {

    var id = document.getElementById("testuri");
    if(id.value!= null && id.value!="") {
        showLoader('#loadtest');
        setActionButtonsEnabled(false);
        $.ajax({
            type: 'POST',
            data: JSON.stringify(id.value),
            dataType: "json",
            url: 'rest/api/loadTests',
            success: function (data, textStatus, jqXHR) {

                if(jqXHR.status == 200){
                    if (data.length > 0) {
                        var tests = "";
                        $.each(data, function (i, item) {
                            tests += item.Test + ";\n";
                        });
                        $('#test').val('');
                        $('#test').val(tests);
                        hideLoader('#loadtest', 'Load from URI');
                        setActionButtonsEnabled(true);
                    } else {

                        $('#notmatch').html("This is not a test expression");

                    }
                }else{
                    showNotification("No test found. Check the URI and the syntax of the RDF file", 'warning');
                    hideLoader('#loadtest', 'Load from URI');
                    setActionButtonsEnabled(true);
                }

            },
            error: function (xhr) {
                showNotification("No test found. Check the syntax of the RDF file", 'danger');
                hideLoader('#loadtest', 'Load from URI');
                setActionButtonsEnabled(true);
            }
        });
    }
}

function loadTestsFromFile() {

    var id = document.getElementById("testfile");
    if(id.value!= null && id.value!="") {
        showLoader('#loadtestfile');
        setActionButtonsEnabled(false);
        var ontologyCode = id.value.replace(/><\/http.*:>/g,'\/>');
        $.ajax({
            type: 'POST',
            data: ontologyCode.replace('/#/g',''),
            dataType: "json",
            url: 'rest/api/loadTestsFromFile',
            success: function (data, textStatus, jqXHR) {
                if(jqXHR.status == 200) {
                    if (data.length > 0) {
                        var tests = "";
                        $.each(data, function (i, item) {
                            tests += item.Test + ";\n";
                        });
                        document.getElementById('testfile').innerHTML = '';
                        $('#test').val('');
                        $('#test').val(tests);

                        hideLoader('#loadtestfile', 'Load from code');
                        setActionButtonsEnabled(true);
                    } else {

                        $('#notmatch').html("This is not a test expression");

                    }
                }else{
                    showNotification("No tests found", 'warning');
                    hideLoader('#loadtestfile', 'Load from code');
                    setActionButtonsEnabled(true);
                }

            },
            error: function (xhr) {
                showNotification(xhr.responseText, 'danger');
                hideLoader('#loadtestfile', 'Load from code');
                setActionButtonsEnabled(true);

            }
        });
    }
}

function  removeOntology(btn) {

    var row = btn.parentNode.parentNode;
    row.parentNode.removeChild(row);

}

function  clearResults() {
    if($("#table") != null)
        $("#table").find("tr:gt(0)").remove();

    if($("#tablemultiple") != null)
        $("#tablemultiple").find("tr:gt(0)").remove();

}



function exportfile(){
    var array = $(this).serializeArray();
    var id = document.getElementsByName("testintable");

    id.forEach(function (item) {
        if(item.value!="") {
            array.push(item.innerText.replace(/<span style="color:red;">/g,"").replace(/<\/span>/g,"").replace(/\^\^xsd:string/g,""));
        }
    });

    window.location="rest/api/export?test="+array;

}

function showRows() {
    index = 1;
    tr = document.getElementById('tr'+index);
    while (tr!=null){
        tr.style.display='';
        index++;
        tr = document.getElementById('tr'+index);
    }
    butt = document.getElementById('remButt').style.display='none';
}

