/**
 * Created by lingzhiyuan on 16/4/20.
 */

var provinceSelect;
var citySelect;
var districtSelect;

var selectedProvinceId;
var selectedCityId ;
var selectedDistrictId;

// 根据城市 id 查出区县列表并替换
var changeDistricts = function (cityId) {
    districtSelect.empty()
    var districts = findDistrictsByCityId(cityId);
    $.each(districts, function (index, val) {
        districtSelect.append($("<option></option>").text(val.title).attr("data-district-id", val.id))
    });
    selectedDistrictId = districtSelect.find("option:selected").data("district-id")
};

// 根据省份 id 查出城市列表并替换
var changeCities = function (provinceId) {
    citySelect.empty()
    var cities = findCitiesByProvinceId(provinceId);
    $.each(cities, function (index, val) {
        citySelect.append($("<option></option>").text(val.title).attr("data-city-id", val.id))
    });
    selectedCityId = citySelect.find("option:selected").data("city-id")
    changeDistricts(selectedCityId)
};

// 替换省份列表
var changeProvinces = function () {
    provinceSelect.empty();
    var provinces = window.provinces;
    $.each(provinces, function (index, val) {
        provinceSelect.append($("<option></option>").text(val.title).attr("data-province-id", val.id))
    });
    selectedProvinceId = provinceSelect.find("option:selected").data("province-id")
    changeCities(selectedProvinceId)
};

var switchDistrict = function () {
    selectedDistrictId = $(this).find('option:selected').data('district-id')
}


var switchCity = function () {
    selectedCityId = $(this).find('option:selected').data('city-id')
    changeDistricts(selectedCityId)
}

var switchProvince = function () {
    selectedProvinceId = $(this).find("option:selected").data("province-id");
    changeCities(selectedProvinceId);
};

var locationModal = function (pw, cs, ds) {
    
    provinceSelect = pw;
    citySelect = cs;
    districtSelect = ds;
    provinceSelect.change(switchProvince);
    citySelect.change(switchCity);
    districtSelect.change(switchDistrict);
}