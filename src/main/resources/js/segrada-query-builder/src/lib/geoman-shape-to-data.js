export default (e) => {
  if (!e || !e.shape || !e.layer) return null

  const shape = {
    shape: e.shape
  }

  switch (e.shape) {
    case 'Circle':
      shape.radius = e.layer.getRadius()
      shape.lat = e.layer.getLatLng().lat
      shape.lng = e.layer.getLatLng().lng
      break
    case 'Rectangle':
      shape.coordinates = simplifyLatLngs([
        e.layer.getBounds().getSouthWest(),
        e.layer.getBounds().getNorthEast()
      ])
      break
    case 'Polygon':
      shape.coordinates = simplifyLatLngs(e.layer.getLatLngs()[0])
      break
  }

  return shape
}

function simplifyLatLngs (latLngs) {
  const result = []

  for (const latLng of latLngs) {
    result.push([latLng.lat, latLng.lng])
  }

  return result
}
