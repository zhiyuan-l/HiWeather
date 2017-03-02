/**
 * Created by lingzhiyuan on 16/4/19.
 */

function Province(id, title, pinyin, ishot){
    this.id = id;
    this.title = title;
    this.pinyin = pinyin;
    this.ishot = ishot;
}

function City(id, title, province, pinyin, ishot) {
    this.id = id;
    this.province = province;
    this.title = title;
    this.pinyin = pinyin;
    this.ishot = ishot;
}

function District(id, title, city, pinyin,pinyin_aqi, ishot) {
    this.id = id;
    this.title = title;
    this.city = city;
    this.pinyin = pinyin;
    this.pinyin_aqi = pinyin_aqi;
    this.ishot = ishot;
}

/**
 * 通过省份 id 在全局变量-城市数组中找到所有对应的项
 * */
function findCitiesByProvinceId(provinceId) {

    var province = findProvinceById(provinceId);
    var cities = window.cities;
    var resultCities = [];
    $.each(cities, function (index, val) {
        if(val.province.id == province.id){
            resultCities.push(val)
        }
    });

    return resultCities
}

/**
 * 通过城市 id 在全局变量-区县数组中找到所有对应的项
 * */
function findDistrictsByCityId(cityId) {

    var city = findCityById(cityId);
    var districts = window.districts;
    var resultDistricts = [];
    $.each(districts, function (index, val) {
        if(val.city.id == city.id){
            resultDistricts.push(val)
        }
    });

    return resultDistricts
}

/**
 * 通过省份 id 在全局变量-省份数组中找到对应的项
 * */
var findProvinceById = function (id) {

    var provinces = window.provinces
    var province = new Province();
    $.each(provinces, function (index, val) {
        if (val.id == id){
            province = val
        }
    });
    return province
};

/**
 * 通过城市 id 在全局变量-城市数组中找到对应的项
 * */
var findCityById = function(id) {

    var cities = window.cities
    var city = new City();
    $.each(cities, function (index, val) {
        if (val.id == id){
            city = val
        }
    });
    return city
};

/**
 * 通过区县 id 在全局变量-区县数组中找到对应的项
 * */
var findDistrictById = function(id) {

    var districts = window.districts
    var district = new District();
    $.each(districts, function (index, val) {
        if (val.id == id){
            district = val
        }
    });
    return district
};

/**
 * 获取省份数据并组装成 Province 对象数组, 然后返回
 * */
var getProvinces = function (url) {
    var provinces = [];
    $.ajax({
        url: url,
        dataType: "json",
        async: false,
        success: function (data) {
            var provinceData = data.data
            $.each(provinceData, function (index, val) {
                provinces.push(new Province(val.id, val.title, val.pinyin, val.ishot))
            })
        },
        error: function () {
            alert("省份数据获取失败!")
        }
    });

    return provinces
};

/**
 * 获取城市数据并组装成 City 对象数组, 然后返回
 * */
var getCities = function(url) {
    var cities = [];
    $.ajax({
        url: url,
        dataType: "json",
        async: false,
        success: function (data) {
            var cityData = data.data
            $.each(cityData, function (index, val) {
                var province = findProvinceById(val.province);
                cities.push(new City(val.id, val.title, province, val.pinyin, val.ishot))
            })
        },
        error: function () {
            alert("城市数据获取失败!")
        }
    });

    return cities
};

/**
 * 获取区县数据并组装成 District 对象数组, 然后返回
 * */
var getDistricts = function (url) {
    var districts = [];
    $.ajax({
        url: url,
        dataType: "json",
        async: false,
        success: function (data) {
            var districtData = data.data
            $.each(districtData, function (index, val) {
                var city  = findCityById(val.city);
                if(val.title == city.title)
                {
                    val.title += "[城区]"
                }
                districts.push(new District(val.id, val.title, city, val.pinyin, val.pinyin_aqi, val.ishot))
            })
        },
        error: function () {
            alert("区县数据获取失败!")
        }
    });

    return districts
};