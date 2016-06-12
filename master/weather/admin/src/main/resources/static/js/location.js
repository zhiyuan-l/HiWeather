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
    this.title = title;
    this.province = province;
    this.pinyin = pinyin;
    this.ishot = ishot;
}

function District(id, title, city, pinyin,pinyin_aqi,longitude,latitude, ishot) {
    this.id = id;
    this.title = title;
    this.city = city;
    this.pinyin = pinyin;
    this.pinyin_aqi = pinyin_aqi;
    this.longitude = longitude;
    this.latitude = latitude
    this.ishot = ishot;
}

var  findProvinceById = function (provinces, id) {

    var province = new Province();
    $.each(provinces, function (index, val) {
        if (val.id == id){
            province = val
        }
    });
    return province
};

var findCityById = function(cities, id) {

    var city = new City();
    $.each(cities, function (index, val) {
        if (val.id == id){
            city = val
        }
    });
    return city
};

var getProvinces = function (url) {
    var provinces = [];
    $.ajax({
        url: url,
        dataType: "json",
        async: false,
        success: function (data) {
            $.each(data, function (index, val) {
                provinces.push(new Province(val.id, val.title, val.pinyin, val.ishot))
            })
        },
        error: function () {
            alert("省份数据获取失败!")
        }
    });

    return provinces
};

var getCities = function(provinces, url) {
    var cities = [];
    $.ajax({
        url: url,
        dataType: "json",
        async: false,
        success: function (data) {
            $.each(data, function (index, val) {
                var province = findProvinceById(provinces, val.province);
                cities.push(new City(val.id, val.title, province, val.pinyin, val.ishot))
            })
        },
        error: function () {
            alert("城市数据获取失败!")
        }
    });

    return cities
};

var getDistricts = function (cities, url) {
    var districts = [];
    $.ajax({
        url: url,
        dataType: "json",
        async: false,
        success: function (data) {
            $.each(data, function (index, val) {
                var city  = findCityById(cities, val.city);
                if(val.title == city.title)
                {
                    val.title = val.title+"[城区]"
                }
                districts.push(new District(val.id, val.title, city, val.pinyin, val.pinyin_aqi,val.longitude, val.latitude, val.ishot))
            })
        },
        error: function () {
            alert("区县数据获取失败!")
        }
    });

    return districts
};