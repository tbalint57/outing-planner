export function buildSelect(selectId, selectName, sourceData, valueName, displayName, defaultSelection) {
    const select = document.createElement('select');
    select.name = selectName;
    select.id = selectId;

    select.innerHTML += '<option disabled selected value>' + defaultSelection + '</option>'

    sourceData.forEach(entry => {
        const opt = document.createElement('option');
        opt.value = entry[valueName];
        opt.innerHTML = "";
        displayName.forEach(index => {
            opt.innerHTML += entry[index] + " ";
        });
        select.appendChild(opt);
    });

    return select;
}

export async function queryServlet(data, service){
    data.append('service', service);

    const response = await fetch('query', {
        method: 'POST',
        body: data
    });

    const result = await response.json();

    if (result.redirect != null){
        window.location.replace(result.redirect);
    }

    return result;
}