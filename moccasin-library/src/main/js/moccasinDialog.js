$("<style type='text/css'>.moccasin-dialog .jstree-node .jstree-anchor {cursor: default}</style>").appendTo("head");
$("<style type='text/css'>.moccasin-dialog .jstree-node.jstree-leaf .jstree-anchor {cursor: pointer; font-weight: bold;}</style>").appendTo("head");
function MoccasinDialog(inputText, url) {
    this.url = url;
    this.index = inputText.getAttribute('data-uuid');
    this.catalog = inputText.getAttribute('data-catalog');
    this.input = inputText;
    this.value = inputText.value;

    var modal = document.createElement('div');

    this.input.readOnly = true;
    this.input.addEventListener("click", this.showModal.bind(this));

    var modalHTML =
    '        <div class="modal fade moccasin-dialog" id="moccasin-dialog-' + this.index + '" tabindex="-1" role="dialog" aria-labelledby="moccasinDialogLabel" aria-hidden="true">\n' +
    '            <div class="modal-dialog" role="document">\n' +
    '                <div class="modal-content" style="width:50em;">\n' +
    '                    <div class="modal-header">\n' +
    '                        <h3 class="modal-title" style="display: inline;" id="moccasinDialogLabel">Katalog</h3>\n' +
    '                        <button type="button" class="close" data-dismiss="modal" aria-label="Close">\n' +
    '                            <span aria-hidden="true">&times;</span>\n' +
    '                        </button>\n' +
    '                    </div>\n' +
    '                    <div class="modal-body">\n' +
    '                        <div class="container-fluid">\n' +
    '                            <div class="row" style="padding: 0 0 1em 0;">\n' +
    '                                <input id="moccasin-tree-' + this.index + '-search" type="text" class="input"/>\n' +
    '                            </div>\n' +
    '                            <div class="row" style="overflow:auto; height: 30em;">\n' +
    '                                <div id="moccasin-tree-' + this.index + '"></div>\n' +
    '                            </div>\n' +
    '                        </div>\n' +
    '                    </div>\n' +
    '                    <div class="modal-footer">\n' +
    '                        <div class="container-fluid">\n' +
    '                            <div class="row">\n' +
    '                                <button name="modal-cancel" type="button" class="btn btn-secondary glyphicon glyphicon-remove" data-dismiss="modal"></button>\n' +
    '                                <button name="modal-accept" type="button" class="btn btn-primary glyphicon glyphicon-ok"></button>\n' +
    '                            </div>\n' +
    '                        </div>\n' +
    '                    </div>\n' +
    '                </div>\n' +
    '            </div>\n' +
    '        </div>';

    $(modal).html(modalHTML);
    $(modal).find("[name='modal-accept']").on("click", this.accept.bind(this));
    document.body.appendChild(modal);

    $('#moccasin-tree-' + this.index).jstree({
        'plugins': ['search'],
        'core': {
            'themes': {
                'icons': false,
                'stripes': true,
                'responsive': true
            },
            'data': {
                'url': this.url + '/data?catalog=' + this.catalog
            }
        },
        'search': {
            'ajax': {
                'url': this.url  + '/search?catalog=' + this.catalog,
                'success': function(result) {
                    $('#moccasin-tree-' + this.index).jstree().settings.core.data = result;
                    $('#moccasin-tree-' + this.index).jstree().refresh();
                    $('#moccasin-tree-' + this.index).jstree().flag = 'open-all';
                }.bind(this)
            }
        }
    }).on("refresh.jstree", function (e, data) {
        if($(this).jstree().flag == 'open-all') $(this).jstree("open_all");
        else if($(this).jstree().flag == 'close-all') $(this).jstree("close_all");
        $(this).jstree().flag = new String();
    }).on("hover_node.jstree", function (e, data) {
        var a = $("li.jstree-leaf > a[id='" + data.node.id + "_anchor']");
        var nodeData = JSON.parse(data.node.data);
        var popoverTable = document.createElement('table');
        popoverTable.className = "table table-striped table-dark table-bordered";
        for (var key in nodeData) {
            if(nodeData.hasOwnProperty(key)) {
                var tr = document.createElement('tr');
                var keyTd = document.createElement('td');
                var valTd = document.createElement('td');
                keyTd.appendChild(document.createTextNode(key));
                valTd.appendChild(document.createTextNode(nodeData[key]));
                tr.appendChild(keyTd);
                tr.appendChild(valTd);
                popoverTable.appendChild(tr);
            }
        }
        $(a).popover({
            'html': true,
            'title': data.node.text.substr(0, data.node.text.indexOf(':')),
            'content': popoverTable,
            'trigger': "hover"
        });
        $(a).popover('show');
    }).on("select_node.jstree", this.setValueFromNode.bind(this));

    document.getElementById('moccasin-tree-' + this.index + '-search').addEventListener('keyup', this.search.bind(this));
}

MoccasinDialog.prototype.showModal = function(event) {
    $('#moccasin-dialog-' + this.index).modal('show');
}

MoccasinDialog.prototype.search = function (e) {
    var val = $('#moccasin-tree-' + this.index +'-search').val();
    if(val.length > 1) {
        $('#moccasin-tree-' + this.index).jstree().search(val);
    }
    else if(val.length == 0) {
        $('#moccasin-tree-' + this.index).jstree().clear_search();
        $('#moccasin-tree-' + this.index).jstree().settings.core.data = {'url': this.url + '/data?catalog=' + this.catalog};
        $('#moccasin-tree-' + this.index).jstree().flag = 'close-all';
        $('#moccasin-tree-' + this.index).jstree().refresh();
    }
}

MoccasinDialog.prototype.accept = function () {
    this.input.value = this.value;
    $('#moccasin-dialog-' + this.index).modal('toggle');
}

MoccasinDialog.prototype.setValueFromNode = function (e, data) {
    if( $('#moccasin-tree-' + this.index).jstree().is_leaf(data.node)) this.value = data.node.text;
}
