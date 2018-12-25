
var CollectionUtils = new function() {
    this.groupBy = function(list, propertyName) {
        var result = [];
        var map = {};
        for (var i = 0; i < list.length; i++) {
            var item = list[i];
            var key = item[propertyName];
            if (map[key] == null) {
                map[key] = result.length;
                result.push([]);
            }
            
            result[map[key]].push(item);
        }
        return result;
    };
};