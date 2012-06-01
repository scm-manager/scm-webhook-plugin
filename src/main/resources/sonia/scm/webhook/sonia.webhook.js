/*
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */


Ext.ns('Sonia.webhook');

Sonia.webhook.ConfigPanel = Ext.extend(Sonia.repository.PropertiesFormPanel, {
  
  webhookStore: null,
  
  // labels
  formTitleText: 'WebHooks',
  colUrlText: 'Url Pattern',
  colEveryCommitText: 'Execute on every commit',
  colSendCommitData: 'Send commit data',
  addText: 'Add',
  removeTest: 'Remove',
  
  // help
  webhookGridHelpText: 'Add and remove WebHooks for your repositories. \n\
    The "Url Pattern" column specifies the url of the remote website. \n\
    You can use patterns like ${repository.name} for the url.\n\
    If you enable the "Execute on every commit" checkbox, then is the specified \n\
    url triggert for each commit in a push. \n\
    If the checkbox is disabled the url is triggert once in a push.',
  
  // icons
  addIcon: 'resources/images/add.gif',
  removeIcon: 'resources/images/delete.gif',
  
  initComponent: function(){
    this.webhookStore = new Ext.data.ArrayStore({
      root: 'webhooks',
      fields: [
        {name: 'urlPattern'},
        {name: 'executeOnEveryCommit'},
        {name: 'sendCommitData'}
      ]
    });
    
    this.loadWebhooks(this.webhookStore, this.item);
    
    var webhookColModel = new Ext.grid.ColumnModel({
      defaults: {
        sortable: false,
        editable: true
      },
      columns: [{
        id: 'urlPattern',
        dataIndex: 'urlPattern',
        header: this.colUrlText,
        editor: Ext.form.TextField
      },{
        id: 'executeOnEveryCommit',
        xtype: 'checkcolumn',
        dataIndex: 'executeOnEveryCommit',
        header: this.colEveryCommitText
      }/*,{
        id: 'sendCommitData',
        xtype: 'checkcolumn',
        dataIndex: 'sendCommitData',
        header: this.colSendCommitData
      }*/]
    });
    
    var selectionModel = new Ext.grid.RowSelectionModel({
      singleSelect: true
    });
    
    var config = {
      title: this.formTitleText,
      items: [{
        id: 'webhookGrid',
        xtype: 'editorgrid',
        clicksToEdit: 1,
        autoExpandColumn: 'uri',
        frame: true,
        width: '100%',
        autoHeight: true,
        autoScroll: false,
        colModel: webhookColModel,
        sm: selectionModel,
        store: this.webhookStore,
        viewConfig: {
          forceFit:true
        },
        tbar: [{
          text: this.addText,
          scope: this,
          icon: this.addIcon,
          handler : function(){
            var WebHook = this.webhookStore.recordType;
            var p = new WebHook();
            var grid = Ext.getCmp('webhookGrid');
            grid.stopEditing();
            this.webhookStore.insert(0, p);
            grid.startEditing(0, 0);
          }
        },{
          text: this.removeText,
          scope: this,
          icon: this.removeIcon,
          handler: function(){
            var grid = Ext.getCmp('webhookGrid');
            var selected = grid.getSelectionModel().getSelected();
            if ( selected ){
              this.webhookStore.remove(selected);
            }
          }
        }, '->',{
          id: 'webhookGridHelp',
          xtype: 'box',
          autoEl: {
            tag: 'img',
            src: 'resources/images/help.gif'
          }
        }]

      }]
    }
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.webhook.ConfigPanel.superclass.initComponent.apply(this, arguments);
  },
  
  afterRender: function(){
    // call super
    Sonia.repository.PropertiesFormPanel.superclass.afterRender.apply(this, arguments);

    Ext.QuickTips.register({
      target: Ext.getCmp('webhookGridHelp'),
      title: '',
      text: this.webhookGridHelpText,
      enabled: true
    });
  },
 
  loadWebhooks: function(store, repository){
    if (debug){
      console.debug('load webhook properties');
    }
    if (!repository.properties){
      repository.properties = [];
    }
    Ext.each(repository.properties, function(prop){
      if ( prop.key == 'webhooks' ){
        var value = prop.value;
        this.parseWebhooks(store, value);
      }
    }, this);
  },
  
  parseWebhooks: function(store, webhookString){
    var parts = webhookString.split('|');
    Ext.each(parts, function(part){
      var pa = part.split(';');
      if ( pa.length > 0 && pa[0].length > 0 ){
        var Webhook = store.recordType;
        var w = new Webhook({
          urlPattern: pa[0].trim()
        });
        if (pa[1]){
          w.executeOnEveryCommit = pa[1];
        } else {
          w.executeOnEveryCommit = false;
        }
        if (pa[2]){
          w.sendCommitData = pa[2];
        } else {
          w.sendCommitData = false;
        }
        if (debug){
          console.debug('add webhook: ');
          console.debug( w );
        }
        store.add(w);
      }
    });
  },
  
  storeExtraProperties: function(repository){
    if (debug){
      console.debug('store webhook properties');
    }
    
    // delete old sub repositories
    Ext.each(repository.properties, function(prop, index){
      if ( prop.key == 'webhooks' ){
        delete repository.properties[index];
      }
    });
    
    var webhookString = '';
    this.webhookStore.data.each(function(r){
      var w = r.data;
      // TODO set sendCommitData
      webhookString += w.urlPattern + ';' + w.executeOnEveryCommit + ';false|';
    });
    
    if (debug){
      console.debug('add webhook string: ' + webhookString);
    }
    
    repository.properties.push({
      key: 'webhooks',
      value: webhookString
    });
  }
  
  
});


// register xtype
Ext.reg("webhookConfigPanel", Sonia.webhook.ConfigPanel);

// register panel
Sonia.repository.openListeners.push(function(repository, panels){
  if (Sonia.repository.isOwner(repository)){
    panels.push({
      xtype: 'webhookConfigPanel',
      item: repository
    });
  }
});
