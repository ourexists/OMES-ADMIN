export function mapExampleHtml(protocol?: string): string {
  const p = (protocol || '').toLowerCase().replace(/[\s_-]/g, '')
  if (p.includes('modbus')) {
    return `<div><strong>Modbus TCP</strong><br/>属性：holding-register:1、40001<br/>运行：holding-register:0<br/>控制：coil:0、holding-register:10</div>`
  }
  if (p.includes('opc') || p.includes('ua')) {
    return `<div><strong>OPC UA</strong><br/>属性：ns=2;s=Machine/Temperature<br/>运行：ns=2;s=Machine/Run<br/>控制：ns=2;s=Machine/Valve</div>`
  }
  if (p.includes('s7')) {
    return `<div><strong>S7</strong><br/>V：VB100、VW100、V100.0；M：MW0、M0.0；DB：DB1.DBW0<br/>运行：VW0、M0.0；控制：Q0.0、VW200</div>`
  }
  if (p.includes('wincc')) {
    return `<div><strong>WINCC</strong><br/>属性：Machine1.Temperature<br/>运行：Process.RunStatus</div>`
  }
  return `<div><strong>REST / MQTT</strong><br/>属性：JSON 键如 temperature、data.status<br/>运行：run、status</div>`
}

export function supportsControlProtocol(protocol?: string): boolean {
  const p = (protocol || '').toLowerCase()
  return p.includes('s7') || p.includes('modbus') || p.includes('opc')
}
